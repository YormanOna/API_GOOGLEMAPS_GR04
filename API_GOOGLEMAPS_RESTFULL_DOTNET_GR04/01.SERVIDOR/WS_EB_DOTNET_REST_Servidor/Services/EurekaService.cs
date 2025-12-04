using Microsoft.Data.SqlClient;
using System.Data;
using System.Security.Cryptography;
using System.Text;
using WS_EB_DOTNET_REST_Servidor.Models;

namespace WS_EB_DOTNET_REST_Servidor.Services
{
    public class EurekaService
    {
        private readonly IConfiguration _config;
        public EurekaService(IConfiguration config) => _config = config;

        private SqlConnection GetConnection()
            => new SqlConnection(_config.GetConnectionString("EUREKABANK")!);

        // ===== LOGIN (igual que SOAP) =====
        private const string USUARIO = "MONSTER";
        private static readonly string PASSWORD_HASH = CrearHash("MONSTER9");

        public bool ValidarIngreso(string usuario, string password)
            => USUARIO.Equals(usuario) && PASSWORD_HASH.Equals(CrearHash(password));

        private static string CrearHash(string password)
        {
            using var sha = SHA256.Create();
            var bytes = sha.ComputeHash(Encoding.UTF8.GetBytes(password));
            return Convert.ToBase64String(bytes);
        }

        // ===== LISTAR MOVIMIENTOS =====
        public IEnumerable<Movimiento> ListarMovimientos(string cuenta)
        {
            var lista = new List<Movimiento>();
            using var cn = GetConnection();
            using var cmd = new SqlCommand(@"
                SELECT chr_cuencodigo, int_movinumero, dtt_movifecha,
                       chr_emplcodigo, chr_tipocodigo, dec_moviimporte, chr_cuenreferencia
                FROM dbo.Movimiento
                WHERE chr_cuencodigo = @cuenta
                ORDER BY dtt_movifecha DESC, int_movinumero DESC;", cn);
            cmd.Parameters.AddWithValue("@cuenta", cuenta);
            cn.Open();
            using var dr = cmd.ExecuteReader();
            while (dr.Read())
            {
                lista.Add(new Movimiento
                {
                    CuentaCodigo = dr.GetString(0),
                    Numero = dr.GetInt32(1),
                    Fecha = dr.GetDateTime(2),
                    EmpleadoCodigo = dr.GetString(3),
                    TipoCodigo = dr.GetString(4),
                    Importe = dr.GetDecimal(5),
                    ReferenciaCuenta = dr.IsDBNull(6) ? null : dr.GetString(6)
                });
            }
            return lista;
        }

        // ===== HELPERS TX =====
        private static int SiguienteMov(SqlConnection cn, SqlTransaction tx, string cuenta)
        {
            using var cmd = new SqlCommand(
                "SELECT ISNULL(MAX(int_movinumero),0)+1 FROM dbo.Movimiento WHERE chr_cuencodigo=@c;",
                cn, tx);
            cmd.Parameters.Add(new SqlParameter("@c", SqlDbType.Char, 8) { Value = cuenta });
            return Convert.ToInt32(cmd.ExecuteScalar());
        }

        private static void VerificarCuentaActiva(SqlConnection cn, SqlTransaction tx, string cuenta)
        {
            using var cmd = new SqlCommand(
                "SELECT 1 FROM dbo.Cuenta WHERE chr_cuencodigo=@c AND vch_cuenestado='ACTIVO';",
                cn, tx);
            cmd.Parameters.Add(new SqlParameter("@c", SqlDbType.Char, 8) { Value = cuenta });
            var ok = cmd.ExecuteScalar();
            if (ok == null) throw new Exception($"La cuenta {cuenta} no existe o no est� activa.");
        }

        private static decimal SaldoActual(SqlConnection cn, SqlTransaction tx, string cuenta)
        {
            using var cmd = new SqlCommand(
                "SELECT dec_cuensaldo FROM dbo.Cuenta WITH (UPDLOCK, ROWLOCK, HOLDLOCK) WHERE chr_cuencodigo=@c;",
                cn, tx);
            cmd.Parameters.Add(new SqlParameter("@c", SqlDbType.Char, 8) { Value = cuenta });
            var r = cmd.ExecuteScalar();
            if (r == null) throw new Exception($"Cuenta {cuenta} no encontrada.");
            return Convert.ToDecimal(r);
        }

        // ===== DEP�SITO: retorna saldo actualizado (como SOAP) =====
        public decimal RegistrarDeposito(string cuenta, decimal importe, string empleado)
        {
            using var cn = GetConnection();
            cn.Open();
            using var tx = cn.BeginTransaction();
            try
            {
                VerificarCuentaActiva(cn, tx, cuenta);
                var nro = SiguienteMov(cn, tx, cuenta);

                decimal nuevoSaldo;
                using (var cmd = new SqlCommand(@"
                    UPDATE dbo.Cuenta
                    SET dec_cuensaldo = dec_cuensaldo + @imp, int_cuencontmov = int_cuencontmov + 1
                    OUTPUT inserted.dec_cuensaldo
                    WHERE chr_cuencodigo=@c AND vch_cuenestado='ACTIVO';", cn, tx))
                {
                    cmd.Parameters.AddWithValue("@imp", importe);
                    cmd.Parameters.AddWithValue("@c", cuenta);
                    var r = cmd.ExecuteScalar();
                    if (r == null) throw new Exception("No se pudo actualizar la cuenta.");
                    nuevoSaldo = Convert.ToDecimal(r);
                }

                using (var cmd = new SqlCommand(@"
                    INSERT INTO dbo.Movimiento
                      (chr_cuencodigo,int_movinumero,dtt_movifecha,chr_emplcodigo,chr_tipocodigo,dec_moviimporte,chr_cuenreferencia)
                    VALUES
                      (@c,@n,GETDATE(),@e,'003',@imp,NULL);", cn, tx))
                {
                    cmd.Parameters.AddWithValue("@c", cuenta);
                    cmd.Parameters.AddWithValue("@n", nro);
                    cmd.Parameters.AddWithValue("@e", string.IsNullOrWhiteSpace(empleado) ? "0001" : empleado);
                    cmd.Parameters.AddWithValue("@imp", importe);
                    cmd.ExecuteNonQuery();
                }

                tx.Commit();
                return Math.Round(nuevoSaldo, 2);
            }
            catch
            {
                tx.Rollback();
                throw;
            }
        }

        // ===== RETIRO: retorna saldo actualizado (como SOAP) =====
        public decimal RegistrarRetiro(string cuenta, decimal importe, string empleado)
        {
            using var cn = GetConnection();
            cn.Open();
            using var tx = cn.BeginTransaction();
            try
            {
                VerificarCuentaActiva(cn, tx, cuenta);
                var saldo = SaldoActual(cn, tx, cuenta);
                if (saldo < importe) throw new Exception("Saldo insuficiente.");
                var nro = SiguienteMov(cn, tx, cuenta);

                decimal nuevoSaldo;
                using (var cmd = new SqlCommand(@"
                    UPDATE dbo.Cuenta
                    SET dec_cuensaldo = dec_cuensaldo - @imp, int_cuencontmov = int_cuencontmov + 1
                    OUTPUT inserted.dec_cuensaldo
                    WHERE chr_cuencodigo=@c AND vch_cuenestado='ACTIVO';", cn, tx))
                {
                    cmd.Parameters.AddWithValue("@imp", importe);
                    cmd.Parameters.AddWithValue("@c", cuenta);
                    var r = cmd.ExecuteScalar();
                    if (r == null) throw new Exception("No se pudo actualizar la cuenta.");
                    nuevoSaldo = Convert.ToDecimal(r);
                }

                using (var cmd = new SqlCommand(@"
                    INSERT INTO dbo.Movimiento
                      (chr_cuencodigo,int_movinumero,dtt_movifecha,chr_emplcodigo,chr_tipocodigo,dec_moviimporte,chr_cuenreferencia)
                    VALUES
                      (@c,@n,GETDATE(),@e,'004',@imp,NULL);", cn, tx))
                {
                    cmd.Parameters.AddWithValue("@c", cuenta);
                    cmd.Parameters.AddWithValue("@n", nro);
                    cmd.Parameters.AddWithValue("@e", string.IsNullOrWhiteSpace(empleado) ? "0004" : empleado);
                    cmd.Parameters.AddWithValue("@imp", importe);
                    cmd.ExecuteNonQuery();
                }

                tx.Commit();
                return Math.Round(nuevoSaldo, 2);
            }
            catch
            {
                tx.Rollback();
                throw;
            }
        }

        // ===== TRANSFERENCIA: retorna saldo actualizado de ORIGEN (como SOAP) =====
        public decimal RegistrarTransferencia(string origen, string destino, decimal importe, string empleado)
        {
            using var cn = GetConnection();
            cn.Open();
            using var tx = cn.BeginTransaction();
            try
            {
                var a = string.CompareOrdinal(origen, destino) <= 0 ? origen : destino;
                var b = a == origen ? destino : origen;

                VerificarCuentaActiva(cn, tx, a);
                VerificarCuentaActiva(cn, tx, b);

                var saldoOri = SaldoActual(cn, tx, origen);
                if (saldoOri < importe) throw new Exception("Saldo insuficiente en cuenta origen.");

                var nroOri = SiguienteMov(cn, tx, origen);
                var nroDes = SiguienteMov(cn, tx, destino);

                decimal saldoOrigenNuevo;
                using (var cmd = new SqlCommand(@"
                    UPDATE dbo.Cuenta
                    SET dec_cuensaldo = dec_cuensaldo - @imp, int_cuencontmov = int_cuencontmov + 1
                    OUTPUT inserted.dec_cuensaldo
                    WHERE chr_cuencodigo=@c AND vch_cuenestado='ACTIVO';", cn, tx))
                {
                    cmd.Parameters.AddWithValue("@imp", importe);
                    cmd.Parameters.AddWithValue("@c", origen);
                    var r = cmd.ExecuteScalar();
                    if (r == null) throw new Exception("No se pudo debitar origen.");
                    saldoOrigenNuevo = Convert.ToDecimal(r);
                }

                using (var cmd = new SqlCommand(@"
                    INSERT INTO dbo.Movimiento
                      (chr_cuencodigo,int_movinumero,dtt_movifecha,chr_emplcodigo,chr_tipocodigo,dec_moviimporte,chr_cuenreferencia)
                    VALUES
                      (@c,@n,GETDATE(),@e,'009',@imp,NULL);", cn, tx))
                {
                    cmd.Parameters.AddWithValue("@c", origen);
                    cmd.Parameters.AddWithValue("@n", nroOri);
                    cmd.Parameters.AddWithValue("@e", string.IsNullOrWhiteSpace(empleado) ? "0004" : empleado);
                    cmd.Parameters.AddWithValue("@imp", importe);
                    cmd.Parameters.AddWithValue("@ref", destino);
                    cmd.ExecuteNonQuery();
                }

                using (var cmd = new SqlCommand(@"
                    UPDATE dbo.Cuenta
                    SET dec_cuensaldo = dec_cuensaldo + @imp, int_cuencontmov = int_cuencontmov + 1
                    WHERE chr_cuencodigo=@c AND vch_cuenestado='ACTIVO';", cn, tx))
                {
                    cmd.Parameters.AddWithValue("@imp", importe);
                    cmd.Parameters.AddWithValue("@c", destino);
                    if (cmd.ExecuteNonQuery() == 0) throw new Exception("No se pudo acreditar destino.");
                }

                using (var cmd = new SqlCommand(@"
                    INSERT INTO dbo.Movimiento
                      (chr_cuencodigo,int_movinumero,dtt_movifecha,chr_emplcodigo,chr_tipocodigo,dec_moviimporte,chr_cuenreferencia)
                    VALUES
                      (@c,@n,GETDATE(),@e,'008',@imp,NULL);", cn, tx))
                {
                    cmd.Parameters.AddWithValue("@c", destino);
                    cmd.Parameters.AddWithValue("@n", nroDes);
                    cmd.Parameters.AddWithValue("@e", string.IsNullOrWhiteSpace(empleado) ? "0004" : empleado);
                    cmd.Parameters.AddWithValue("@imp", importe);
                    cmd.Parameters.AddWithValue("@ref", origen);
                    cmd.ExecuteNonQuery();
                }

                tx.Commit();
                return Math.Round(saldoOrigenNuevo, 2); // saldo de ORIGEN
            }
            catch
            {
                tx.Rollback();
                throw;
            }
        }

        // ========================================
        // ===== CRUD SUCURSALES =====
        // ========================================

        // Listar sucursales activas (solo las que tienen bit_sucuisactive = 1)
        public IEnumerable<Sucursal> ListarSucursales()
        {
            var lista = new List<Sucursal>();
            using var cn = GetConnection();
            using var cmd = new SqlCommand(@"
                SELECT chr_sucucodigo, vch_sucunombre, vch_sucuciudad, vch_sucudireccion,
                       int_sucucontcuenta, dec_sululatitud, dec_suculongitud, bit_sucuisactive
                FROM dbo.Sucursal
                WHERE bit_sucuisactive = 1
                ORDER BY chr_sucucodigo;", cn);
            cn.Open();
            using var dr = cmd.ExecuteReader();
            while (dr.Read())
            {
                lista.Add(new Sucursal
                {
                    Codigo = dr.GetString(0).TrimEnd(),
                    Nombre = dr.GetString(1),
                    Ciudad = dr.GetString(2),
                    Direccion = dr.IsDBNull(3) ? null : dr.GetString(3),
                    ContadorCuenta = dr.GetInt32(4),
                    Latitud = dr.IsDBNull(5) ? null : dr.GetDecimal(5),
                    Longitud = dr.IsDBNull(6) ? null : dr.GetDecimal(6),
                    IsActive = dr.GetBoolean(7)
                });
            }
            return lista;
        }

        // Obtener una sucursal por código
        public Sucursal? ObtenerSucursal(string codigo)
        {
            using var cn = GetConnection();
            using var cmd = new SqlCommand(@"
                SELECT chr_sucucodigo, vch_sucunombre, vch_sucuciudad, vch_sucudireccion,
                       int_sucucontcuenta, dec_sululatitud, dec_suculongitud, bit_sucuisactive
                FROM dbo.Sucursal
                WHERE chr_sucucodigo = @codigo AND bit_sucuisactive = 1;", cn);
            cmd.Parameters.AddWithValue("@codigo", codigo);
            cn.Open();
            using var dr = cmd.ExecuteReader();
            if (dr.Read())
            {
                return new Sucursal
                {
                    Codigo = dr.GetString(0).TrimEnd(),
                    Nombre = dr.GetString(1),
                    Ciudad = dr.GetString(2),
                    Direccion = dr.IsDBNull(3) ? null : dr.GetString(3),
                    ContadorCuenta = dr.GetInt32(4),
                    Latitud = dr.IsDBNull(5) ? null : dr.GetDecimal(5),
                    Longitud = dr.IsDBNull(6) ? null : dr.GetDecimal(6),
                    IsActive = dr.GetBoolean(7)
                };
            }
            return null;
        }

        // Crear una nueva sucursal
        public bool CrearSucursal(Sucursal sucursal)
        {
            using var cn = GetConnection();
            using var cmd = new SqlCommand(@"
                INSERT INTO dbo.Sucursal 
                    (chr_sucucodigo, vch_sucunombre, vch_sucuciudad, vch_sucudireccion, 
                     int_sucucontcuenta, dec_sululatitud, dec_suculongitud, bit_sucuisactive)
                VALUES 
                    (@codigo, @nombre, @ciudad, @direccion, @contador, @latitud, @longitud, 1);", cn);
            
            cmd.Parameters.Add(new SqlParameter("@codigo", SqlDbType.Char, 3) { Value = sucursal.Codigo });
            cmd.Parameters.AddWithValue("@nombre", sucursal.Nombre);
            cmd.Parameters.AddWithValue("@ciudad", sucursal.Ciudad);
            cmd.Parameters.AddWithValue("@direccion", (object?)sucursal.Direccion ?? DBNull.Value);
            cmd.Parameters.AddWithValue("@contador", sucursal.ContadorCuenta);
            cmd.Parameters.AddWithValue("@latitud", (object?)sucursal.Latitud ?? DBNull.Value);
            cmd.Parameters.AddWithValue("@longitud", (object?)sucursal.Longitud ?? DBNull.Value);
            
            cn.Open();
            return cmd.ExecuteNonQuery() > 0;
        }

        // Actualizar una sucursal existente
        public bool ActualizarSucursal(string codigo, Sucursal sucursal)
        {
            using var cn = GetConnection();
            using var cmd = new SqlCommand(@"
                UPDATE dbo.Sucursal
                SET vch_sucunombre = @nombre,
                    vch_sucuciudad = @ciudad,
                    vch_sucudireccion = @direccion,
                    int_sucucontcuenta = @contador,
                    dec_sululatitud = @latitud,
                    dec_suculongitud = @longitud
                WHERE chr_sucucodigo = @codigo AND bit_sucuisactive = 1;", cn);
            
            cmd.Parameters.Add(new SqlParameter("@codigo", SqlDbType.Char, 3) { Value = codigo });
            cmd.Parameters.AddWithValue("@nombre", sucursal.Nombre);
            cmd.Parameters.AddWithValue("@ciudad", sucursal.Ciudad);
            cmd.Parameters.AddWithValue("@direccion", (object?)sucursal.Direccion ?? DBNull.Value);
            cmd.Parameters.AddWithValue("@contador", sucursal.ContadorCuenta);
            cmd.Parameters.AddWithValue("@latitud", (object?)sucursal.Latitud ?? DBNull.Value);
            cmd.Parameters.AddWithValue("@longitud", (object?)sucursal.Longitud ?? DBNull.Value);
            
            cn.Open();
            return cmd.ExecuteNonQuery() > 0;
        }

        // Eliminación lógica de una sucursal (marca bit_sucuisactive = 0)
        public bool EliminarSucursal(string codigo)
        {
            using var cn = GetConnection();
            using var cmd = new SqlCommand(@"
                UPDATE dbo.Sucursal
                SET bit_sucuisactive = 0
                WHERE chr_sucucodigo = @codigo AND bit_sucuisactive = 1;", cn);
            
            cmd.Parameters.Add(new SqlParameter("@codigo", SqlDbType.Char, 3) { Value = codigo });
            
            cn.Open();
            return cmd.ExecuteNonQuery() > 0;
        }
    }
}
