package ec.edu.monster.servicio;

import ec.edu.monster.db.AccesoDB;
import ec.edu.monster.modelo.Movimiento;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class EurekaService {

    // LOGIN
    private static final String USUARIO = "MONSTER";
    private static final String PASSWORD = generarHash("MONSTER9");

    public Boolean validarIngreso(String usuario, String password) {
        String hashIngresado = generarHash(password);
        return USUARIO.equals(usuario) && PASSWORD.equals(hashIngresado);
    }

    private static String generarHash(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al generar el hash SHA-256", e);
        }
    }

    public List<Movimiento> leerMovimientos(String cuenta) {
        Connection cn = null;
        List<Movimiento> lista = new ArrayList<>();
        String sql = "SELECT \n"
                + " m.chr_cuencodigo cuenta, \n"
                + " m.int_movinumero nromov, \n"
                + " m.dtt_movifecha fecha, \n"
                + " t.vch_tipodescripcion tipo, \n"
                + " t.vch_tipoaccion accion, \n"
                + " m.dec_moviimporte importe \n"
                + "FROM tipomovimiento t INNER JOIN movimiento m \n"
                + "ON t.chr_tipocodigo = m.chr_tipocodigo \n"
                + "WHERE m.chr_cuencodigo = ? \n"
                + "ORDER BY m.dtt_movifecha DESC, m.int_movinumero DESC";

        try {
            cn = AccesoDB.getConnection();
            PreparedStatement pstm = cn.prepareStatement(sql);
            pstm.setString(1, cuenta);
            ResultSet rs = pstm.executeQuery();

            while (rs.next()) {
                Movimiento rec = new Movimiento();
                rec.setCuenta(rs.getString("cuenta"));
                rec.setNromov(rs.getInt("nromov"));
                rec.setFecha(rs.getDate("fecha"));
                rec.setTipo(rs.getString("tipo"));
                rec.setAccion(rs.getString("accion"));
                rec.setImporte(rs.getDouble("importe"));

                lista.add(rec);
            }
            rs.close();

        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            try { if (cn != null) cn.close(); } catch (Exception e) {}
        }
        return lista;
    }

    public double registrarDeposito(String cuenta, double importe, String codEmp) {
        Connection cn = null;
        double saldo;
        try {
            cn = AccesoDB.getConnection();
            cn.setAutoCommit(false);

            String sql = "select dec_cuensaldo, int_cuencontmov "
                    + "from cuenta "
                    + "where chr_cuencodigo = ? and vch_cuenestado = 'ACTIVO' "
                    + "for update";
            PreparedStatement pstm = cn.prepareStatement(sql);
            pstm.setString(1, cuenta);
            ResultSet rs = pstm.executeQuery();
            if (!rs.next()) {
                throw new SQLException("ERROR, cuenta no existe, o no esta activa");
            }
            saldo = rs.getDouble("dec_cuensaldo");
            int cont = rs.getInt("int_cuencontmov");
            rs.close();
            pstm.close();

            saldo += importe;
            cont++;
            sql = "update cuenta "
                    + "set dec_cuensaldo = ?, "
                    + "int_cuencontmov = ? "
                    + "where chr_cuencodigo = ? and vch_cuenestado = 'ACTIVO'";
            pstm = cn.prepareStatement(sql);
            pstm.setDouble(1, saldo);
            pstm.setInt(2, cont);
            pstm.setString(3, cuenta);
            pstm.executeUpdate();
            pstm.close();

            sql = "insert into movimiento(chr_cuencodigo,"
                    + "int_movinumero,dtt_movifecha,chr_emplcodigo,chr_tipocodigo,"
                    + "dec_moviimporte) values(?,?,SYSDATE(),?,'003',?)";
            pstm = cn.prepareStatement(sql);
            pstm.setString(1, cuenta);
            pstm.setInt(2, cont);
            pstm.setString(3, codEmp);
            pstm.setDouble(4, importe);
            pstm.executeUpdate();

            cn.commit();
        } catch (SQLException e) {
            try { if (cn != null) cn.rollback(); } catch (Exception el) {}
            throw new RuntimeException(e.getMessage());
        } catch (Exception e) {
            try { if (cn != null) cn.rollback(); } catch (Exception el) {}
            throw new RuntimeException("ERROR, en el proceso registrar deposito, intentelo mas tarde.");
        } finally {
            try { if (cn != null) cn.close(); } catch (Exception e) {}
        }
        return saldo;
    }

    public double registrarRetiro(String cuenta, double importe, String codEmp) {
        Connection cn = null;
        double saldo;
        try {
            cn = AccesoDB.getConnection();
            cn.setAutoCommit(false);

            String sql = "select dec_cuensaldo, int_cuencontmov "
                    + "from cuenta "
                    + "where chr_cuencodigo = ? and vch_cuenestado = 'ACTIVO' "
                    + "for update";
            PreparedStatement pstm = cn.prepareStatement(sql);
            pstm.setString(1, cuenta);
            ResultSet rs = pstm.executeQuery();

            if (!rs.next()) {
                throw new SQLException("ERROR: La cuenta no existe o no está activa.");
            }

            saldo = rs.getDouble("dec_cuensaldo");
            int cont = rs.getInt("int_cuencontmov");
            rs.close();
            pstm.close();

            if (saldo < importe) {
                throw new SQLException("ERROR: Saldo insuficiente.");
            }

            saldo -= importe;
            cont++;
            sql = "update cuenta set dec_cuensaldo = ?, int_cuencontmov = ? "
                    + "where chr_cuencodigo = ? and vch_cuenestado = 'ACTIVO'";
            pstm = cn.prepareStatement(sql);
            pstm.setDouble(1, saldo);
            pstm.setInt(2, cont);
            pstm.setString(3, cuenta);
            pstm.executeUpdate();
            pstm.close();

            sql = "insert into movimiento(chr_cuencodigo, int_movinumero, dtt_movifecha, chr_emplcodigo, chr_tipocodigo, dec_moviimporte) "
                    + "values (?, ?, SYSDATE(), ?, '004', ?)";
            pstm = cn.prepareStatement(sql);
            pstm.setString(1, cuenta);
            pstm.setInt(2, cont);
            pstm.setString(3, codEmp);
            pstm.setDouble(4, importe);
            pstm.executeUpdate();
            cn.commit();

        } catch (SQLException e) {
            try { if (cn != null) cn.rollback(); } catch (SQLException ex) {}
            throw new RuntimeException(e.getMessage());
        } finally {
            try { if (cn != null) cn.close(); } catch (SQLException e) {}
        }
        return saldo;
    }

    public double registrarTransferencia(String cuentaOrigen, String cuentaDestino, double importe, String codEmp) {
        Connection cn = null;
        try {
            cn = AccesoDB.getConnection();
            cn.setAutoCommit(false);

            // 1. ORIGEN
            String sql = "select dec_cuensaldo, int_cuencontmov "
                    + "from cuenta "
                    + "where chr_cuencodigo = ? and vch_cuenestado = 'ACTIVO' "
                    + "for update";
            PreparedStatement pstm = cn.prepareStatement(sql);
            pstm.setString(1, cuentaOrigen);
            ResultSet rs = pstm.executeQuery();

            if (!rs.next()) {
                throw new SQLException("ERROR: La cuenta origen no existe o no está activa.");
            }

            double saldoOri = rs.getDouble("dec_cuensaldo");
            int contOri = rs.getInt("int_cuencontmov");
            rs.close();
            pstm.close();

            if (saldoOri < importe) {
                throw new SQLException("ERROR: Saldo insuficiente en cuenta origen.");
            }

            // 2. DESTINO
            sql = "select dec_cuensaldo, int_cuencontmov "
                    + "from cuenta "
                    + "where chr_cuencodigo = ? and vch_cuenestado = 'ACTIVO' "
                    + "for update";
            pstm = cn.prepareStatement(sql);
            pstm.setString(1, cuentaDestino);
            rs = pstm.executeQuery();

            if (!rs.next()) {
                throw new SQLException("ERROR: La cuenta destino no existe o no está activa.");
            }

            double saldoDes = rs.getDouble("dec_cuensaldo");
            int contDes = rs.getInt("int_cuencontmov");
            rs.close();
            pstm.close();

            // 3. Actualizar saldos
            saldoOri -= importe;
            contOri++;

            saldoDes += importe;
            contDes++;

            // ORIGEN
            sql = "update cuenta set dec_cuensaldo = ?, int_cuencontmov = ? "
                    + "where chr_cuencodigo = ? and vch_cuenestado = 'ACTIVO'";
            pstm = cn.prepareStatement(sql);
            pstm.setDouble(1, saldoOri);
            pstm.setInt(2, contOri);
            pstm.setString(3, cuentaOrigen);
            pstm.executeUpdate();
            pstm.close();

            // DESTINO
            sql = "update cuenta set dec_cuensaldo = ?, int_cuencontmov = ? "
                    + "where chr_cuencodigo = ? and vch_cuenestado = 'ACTIVO'";
            pstm = cn.prepareStatement(sql);
            pstm.setDouble(1, saldoDes);
            pstm.setInt(2, contDes);
            pstm.setString(3, cuentaDestino);
            pstm.executeUpdate();
            pstm.close();

            // Mov ORIGEN
            sql = "insert into movimiento(chr_cuencodigo, int_movinumero, dtt_movifecha, "
                    + "chr_emplcodigo, chr_tipocodigo, dec_moviimporte) "
                    + "values (?, ?, SYSDATE(), ?, '009', ?)";
            pstm = cn.prepareStatement(sql);
            pstm.setString(1, cuentaOrigen);
            pstm.setInt(2, contOri);
            pstm.setString(3, codEmp);
            pstm.setDouble(4, importe);
            pstm.executeUpdate();
            pstm.close();

            // Mov DESTINO
            sql = "insert into movimiento(chr_cuencodigo, int_movinumero, dtt_movifecha, "
                    + "chr_emplcodigo, chr_tipocodigo, dec_moviimporte) "
                    + "values (?, ?, SYSDATE(), ?, '008', ?)";
            pstm = cn.prepareStatement(sql);
            pstm.setString(1, cuentaDestino);
            pstm.setInt(2, contDes);
            pstm.setString(3, codEmp);
            pstm.setDouble(4, importe);
            pstm.executeUpdate();
            pstm.close();

            cn.commit();

            return saldoOri;

        } catch (SQLException e) {
            try { if (cn != null) cn.rollback(); } catch (Exception ex) {}
            throw new RuntimeException(e.getMessage());
        } catch (Exception e) {
            try { if (cn != null) cn.rollback(); } catch (Exception ex) {}
            throw new RuntimeException("ERROR en el proceso registrar transferencia, intentelo mas tarde.");
        } finally {
            try { if (cn != null) cn.close(); } catch (Exception e) {}
        }
    }

    // =============================================
    // CRUD SUCURSALES
    // =============================================

    /**
     * Lista todas las sucursales activas
     * @return Lista de sucursales
     */
    public List<ec.edu.monster.modelo.Sucursal> listarSucursales() {
        Connection cn = null;
        List<ec.edu.monster.modelo.Sucursal> lista = new ArrayList<>();
        String sql = "SELECT chr_sucucodigo, vch_sucunombre, vch_sucuciudad, "
                + "vch_sucudireccion, int_sucucontcuenta, dec_sululatitud, "
                + "dec_suculongitud, bit_sucuisactive "
                + "FROM sucursal "
                + "WHERE bit_sucuisactive = TRUE "
                + "ORDER BY vch_sucunombre";

        try {
            cn = AccesoDB.getConnection();
            PreparedStatement pstm = cn.prepareStatement(sql);
            ResultSet rs = pstm.executeQuery();

            while (rs.next()) {
                ec.edu.monster.modelo.Sucursal sucursal = new ec.edu.monster.modelo.Sucursal();
                sucursal.setCodigo(rs.getString("chr_sucucodigo"));
                sucursal.setNombre(rs.getString("vch_sucunombre"));
                sucursal.setCiudad(rs.getString("vch_sucuciudad"));
                sucursal.setDireccion(rs.getString("vch_sucudireccion"));
                sucursal.setContadorCuentas(rs.getInt("int_sucucontcuenta"));
                sucursal.setLatitud(rs.getObject("dec_sululatitud", Double.class));
                sucursal.setLongitud(rs.getObject("dec_suculongitud", Double.class));
                sucursal.setActive(rs.getBoolean("bit_sucuisactive"));
                lista.add(sucursal);
            }
            rs.close();

        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            try { if (cn != null) cn.close(); } catch (Exception e) {}
        }
        return lista;
    }

    /**
     * Obtiene una sucursal por su código
     * @param codigo Código de la sucursal
     * @return Sucursal encontrada
     */
    public ec.edu.monster.modelo.Sucursal obtenerSucursal(String codigo) {
        Connection cn = null;
        ec.edu.monster.modelo.Sucursal sucursal = null;
        String sql = "SELECT chr_sucucodigo, vch_sucunombre, vch_sucuciudad, "
                + "vch_sucudireccion, int_sucucontcuenta, dec_sululatitud, "
                + "dec_suculongitud, bit_sucuisactive "
                + "FROM sucursal "
                + "WHERE chr_sucucodigo = ? AND bit_sucuisactive = TRUE";

        try {
            cn = AccesoDB.getConnection();
            PreparedStatement pstm = cn.prepareStatement(sql);
            pstm.setString(1, codigo);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                sucursal = new ec.edu.monster.modelo.Sucursal();
                sucursal.setCodigo(rs.getString("chr_sucucodigo"));
                sucursal.setNombre(rs.getString("vch_sucunombre"));
                sucursal.setCiudad(rs.getString("vch_sucuciudad"));
                sucursal.setDireccion(rs.getString("vch_sucudireccion"));
                sucursal.setContadorCuentas(rs.getInt("int_sucucontcuenta"));
                sucursal.setLatitud(rs.getObject("dec_sululatitud", Double.class));
                sucursal.setLongitud(rs.getObject("dec_suculongitud", Double.class));
                sucursal.setActive(rs.getBoolean("bit_sucuisactive"));
            } else {
                throw new SQLException("ERROR: Sucursal no encontrada o no está activa.");
            }
            rs.close();

        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            try { if (cn != null) cn.close(); } catch (Exception e) {}
        }
        return sucursal;
    }

    /**
     * Crea una nueva sucursal
     * @param sucursal Datos de la sucursal a crear
     * @return Sucursal creada
     */
    public ec.edu.monster.modelo.Sucursal crearSucursal(ec.edu.monster.modelo.Sucursal sucursal) {
        Connection cn = null;
        try {
            System.out.println("=== BACKEND: Iniciando creación de sucursal ===");
            System.out.println("Datos recibidos:");
            System.out.println("  Código: " + sucursal.getCodigo());
            System.out.println("  Nombre: " + sucursal.getNombre());
            System.out.println("  Ciudad: " + sucursal.getCiudad());
            System.out.println("  Dirección: " + sucursal.getDireccion());
            System.out.println("  Contador cuentas: " + sucursal.getContadorCuentas());
            System.out.println("  Latitud: " + sucursal.getLatitud());
            System.out.println("  Longitud: " + sucursal.getLongitud());
            
            // Validaciones
            if (sucursal.getCodigo() == null || sucursal.getCodigo().trim().isEmpty()) {
                throw new RuntimeException("ERROR: El código de la sucursal es obligatorio.");
            }
            if (sucursal.getCodigo().length() > 3) {
                throw new RuntimeException("ERROR: El código de la sucursal debe tener máximo 3 caracteres.");
            }
            if (sucursal.getNombre() == null || sucursal.getNombre().trim().isEmpty()) {
                throw new RuntimeException("ERROR: El nombre de la sucursal es obligatorio.");
            }
            if (sucursal.getCiudad() == null || sucursal.getCiudad().trim().isEmpty()) {
                throw new RuntimeException("ERROR: La ciudad es obligatoria.");
            }
            System.out.println("Validaciones OK");
            
            // Validar coordenadas si están presentes
            if (sucursal.getLatitud() != null) {
                if (sucursal.getLatitud() < -90 || sucursal.getLatitud() > 90) {
                    throw new RuntimeException("ERROR: La latitud debe estar entre -90 y 90.");
                }
            }
            if (sucursal.getLongitud() != null) {
                if (sucursal.getLongitud() < -180 || sucursal.getLongitud() > 180) {
                    throw new RuntimeException("ERROR: La longitud debe estar entre -180 y 180.");
                }
            }

            cn = AccesoDB.getConnection();
            String sql = "INSERT INTO sucursal (chr_sucucodigo, vch_sucunombre, vch_sucuciudad, "
                    + "vch_sucudireccion, int_sucucontcuenta, dec_sululatitud, dec_suculongitud, "
                    + "bit_sucuisactive) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement pstm = cn.prepareStatement(sql);
            pstm.setString(1, sucursal.getCodigo());
            pstm.setString(2, sucursal.getNombre());
            pstm.setString(3, sucursal.getCiudad());
            pstm.setString(4, sucursal.getDireccion());
            pstm.setInt(5, sucursal.getContadorCuentas());
            
            if (sucursal.getLatitud() != null) {
                pstm.setDouble(6, sucursal.getLatitud());
            } else {
                pstm.setNull(6, java.sql.Types.DECIMAL);
            }
            
            if (sucursal.getLongitud() != null) {
                pstm.setDouble(7, sucursal.getLongitud());
            } else {
                pstm.setNull(7, java.sql.Types.DECIMAL);
            }
            
            pstm.setBoolean(8, true);
            
            System.out.println("Ejecutando INSERT SQL...");
            int rowsAffected = pstm.executeUpdate();
            System.out.println("Filas insertadas: " + rowsAffected);
            pstm.close();
            
            System.out.println("=== BACKEND: Sucursal creada exitosamente ===");

        } catch (SQLException e) {
            System.err.println("ERROR SQL al crear sucursal: " + e.getMessage());
            e.printStackTrace();
            if (e.getMessage().contains("Duplicate entry")) {
                throw new RuntimeException("ERROR: Ya existe una sucursal con ese código.");
            }
            throw new RuntimeException(e.getMessage());
        } finally {
            try { if (cn != null) cn.close(); } catch (Exception e) {}
        }
        return sucursal;
    }

    /**
     * Actualiza una sucursal existente
     * @param codigo Código de la sucursal a actualizar
     * @param sucursal Nuevos datos de la sucursal
     * @return Sucursal actualizada
     */
    public ec.edu.monster.modelo.Sucursal actualizarSucursal(String codigo, ec.edu.monster.modelo.Sucursal sucursal) {
        Connection cn = null;
        try {
            // Validaciones
            if (sucursal.getNombre() == null || sucursal.getNombre().trim().isEmpty()) {
                throw new RuntimeException("ERROR: El nombre de la sucursal es obligatorio.");
            }
            if (sucursal.getCiudad() == null || sucursal.getCiudad().trim().isEmpty()) {
                throw new RuntimeException("ERROR: La ciudad es obligatoria.");
            }
            
            // Validar coordenadas si están presentes
            if (sucursal.getLatitud() != null) {
                if (sucursal.getLatitud() < -90 || sucursal.getLatitud() > 90) {
                    throw new RuntimeException("ERROR: La latitud debe estar entre -90 y 90.");
                }
            }
            if (sucursal.getLongitud() != null) {
                if (sucursal.getLongitud() < -180 || sucursal.getLongitud() > 180) {
                    throw new RuntimeException("ERROR: La longitud debe estar entre -180 y 180.");
                }
            }

            cn = AccesoDB.getConnection();
            
            // Verificar que la sucursal existe y está activa
            String sqlCheck = "SELECT chr_sucucodigo FROM sucursal "
                    + "WHERE chr_sucucodigo = ? AND bit_sucuisactive = TRUE";
            PreparedStatement pstmCheck = cn.prepareStatement(sqlCheck);
            pstmCheck.setString(1, codigo);
            ResultSet rs = pstmCheck.executeQuery();
            if (!rs.next()) {
                throw new SQLException("ERROR: Sucursal no encontrada o no está activa.");
            }
            rs.close();
            pstmCheck.close();

            String sql = "UPDATE sucursal SET vch_sucunombre = ?, vch_sucuciudad = ?, "
                    + "vch_sucudireccion = ?, dec_sululatitud = ?, dec_suculongitud = ? "
                    + "WHERE chr_sucucodigo = ? AND bit_sucuisactive = TRUE";
            
            PreparedStatement pstm = cn.prepareStatement(sql);
            pstm.setString(1, sucursal.getNombre());
            pstm.setString(2, sucursal.getCiudad());
            pstm.setString(3, sucursal.getDireccion());
            
            if (sucursal.getLatitud() != null) {
                pstm.setDouble(4, sucursal.getLatitud());
            } else {
                pstm.setNull(4, java.sql.Types.DECIMAL);
            }
            
            if (sucursal.getLongitud() != null) {
                pstm.setDouble(5, sucursal.getLongitud());
            } else {
                pstm.setNull(5, java.sql.Types.DECIMAL);
            }
            
            pstm.setString(6, codigo);
            
            int filasAfectadas = pstm.executeUpdate();
            pstm.close();

            if (filasAfectadas == 0) {
                throw new SQLException("ERROR: No se pudo actualizar la sucursal.");
            }

            sucursal.setCodigo(codigo);

        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            try { if (cn != null) cn.close(); } catch (Exception e) {}
        }
        return sucursal;
    }

    /**
     * Elimina lógicamente una sucursal (soft delete)
     * @param codigo Código de la sucursal a eliminar
     */
    public void eliminarSucursal(String codigo) {
        Connection cn = null;
        try {
            cn = AccesoDB.getConnection();
            
            // Verificar que la sucursal existe y está activa
            String sqlCheck = "SELECT chr_sucucodigo FROM sucursal "
                    + "WHERE chr_sucucodigo = ? AND bit_sucuisactive = TRUE";
            PreparedStatement pstmCheck = cn.prepareStatement(sqlCheck);
            pstmCheck.setString(1, codigo);
            ResultSet rs = pstmCheck.executeQuery();
            if (!rs.next()) {
                throw new SQLException("ERROR: Sucursal no encontrada o ya está eliminada.");
            }
            rs.close();
            pstmCheck.close();

            String sql = "UPDATE sucursal SET bit_sucuisactive = FALSE "
                    + "WHERE chr_sucucodigo = ?";
            
            PreparedStatement pstm = cn.prepareStatement(sql);
            pstm.setString(1, codigo);
            
            int filasAfectadas = pstm.executeUpdate();
            pstm.close();

            if (filasAfectadas == 0) {
                throw new SQLException("ERROR: No se pudo eliminar la sucursal.");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            try { if (cn != null) cn.close(); } catch (Exception e) {}
        }
    }
}
