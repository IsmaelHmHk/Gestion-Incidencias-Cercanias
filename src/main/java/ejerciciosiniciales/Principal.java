package ejerciciosiniciales;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Scanner;

/**
 * Práctica Evaluable RA2 (Criterio f)
 * Módulo: Acceso a Datos (DAM 2º)
 * Alumno: Ismael Hmaimou
 * Proyecto: RenfeAlertas v2 - Gestión de modificaciones con control de integridad referencial
 */
public class Principal {

	private static final String URL = "jdbc:mysql://localhost:3306/renfe_alertas";
	private static final String USER = "root";
	private static final String PASS = "abcd*1234";


	public static void main(String[] args) {
		modificar_bd();
	}


	public static void modificar_bd() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println("[ERROR DE CONFIGURACIÓN] No se encontró el driver JDBC de MySQL.");
			return;
		}

		Scanner sc = new Scanner(System.in);
		int opcion = -1;

		do {
			System.out.println("\n=======================================================");
			System.out.println("   GESTIÓN DE BD - RENFEALERTAS v2 (INTEGRIDAD MYSQL)");
			System.out.println("=======================================================");
			System.out.println("1. Inserción (INSERT)");
			System.out.println("2. Eliminación (DELETE)");
			System.out.println("3. Actualización (UPDATE)");
			System.out.println("4. Consultar registros actuales (SELECT)");
			System.out.println("0. Salir");
			System.out.print("Elige una opción: ");

			try {
				opcion = Integer.parseInt(sc.nextLine().trim());
			} catch (NumberFormatException e) {
				System.out.println("[AVISO] Entrada no válida. Introduce un número del 0 al 4.");
				continue;
			}

			switch (opcion) {
				case 1:
					insertarRegistro(sc);
					break;
				case 2:
					eliminarRegistro(sc);
					break;
				case 3:
					actualizarRegistro(sc);
					break;
				case 4:
					mostrarDatos();
					break;
				case 0:
					System.out.println("Cerrando aplicación. ¡Hasta pronto!");
					break;
				default:
					System.out.println("[AVISO] Opción incorrecta. Selecciona entre 0 y 4.");
					break;
			}
		} while (opcion != 0);

		sc.close();
	}

	/**
	 * Opción 1: Inserción en tabla padre (usuario) o tabla hija (incidencia).
	 * Permite probar errores: 1062 (duplicado), 1048 (NOT NULL), 3819 (CHECK) y 1452 (FK padre inexistente).
	 */
	private static void insertarRegistro(Scanner sc) {
		System.out.println("\n--- INSERCIÓN DE REGISTROS ---");



			String sql = "INSERT INTO usuario (id_usuario, nombre, email, password_hash, rol) VALUES (?, ?, ?, ?, ?)";
			try (Connection con = DriverManager.getConnection(URL, USER, PASS);
				 PreparedStatement ps = con.prepareStatement(sql)) {

				System.out.print("ID de usuario (0 para autoincremento, o un ID existente para probar Error 1062): ");
				int id = Integer.parseInt(sc.nextLine().trim());
				if (id == 0) {
					ps.setNull(1, Types.INTEGER);
				} else {
					ps.setInt(1, id);
				}

				System.out.print("Nombre (escribe NULL para probar Error 1048): ");
				String nombre = sc.nextLine().trim();
				if (nombre.equalsIgnoreCase("NULL")) {
					ps.setNull(2, Types.VARCHAR);
				} else {
					ps.setString(2, nombre);
				}

				System.out.print("Email (escribe un email sin '@' para probar Error 3819, o uno repetido para 1062): ");
				String email = sc.nextLine().trim();
				if (email.equalsIgnoreCase("NULL")) {
					ps.setNull(3, Types.VARCHAR);
				} else {
					ps.setString(3, email);
				}

				ps.setString(4, "hash_demo_123");

				System.out.print("Rol (VIAJERO / EMPLEADO / ADMINISTRADOR): ");
				String rol = sc.nextLine().trim().toUpperCase();
				ps.setString(5, rol.isEmpty() ? "VIAJERO" : rol);

				int filas = ps.executeUpdate();
				System.out.println("[OK] Usuario insertado correctamente (" + filas + " fila afectada).");

			} catch (NumberFormatException e) {
				System.out.println("[AVISO] El ID debe ser un número entero.");
			} catch (SQLException e) {
				gestionarErrorSQL(e);
			}


	}


	private static void eliminarRegistro(Scanner sc) {
		System.out.println("\n--- ELIMINACIÓN DE USUARIO ---");
		String sql = "DELETE FROM usuario WHERE id_usuario = ?";

		try (Connection con = DriverManager.getConnection(URL, USER, PASS);
			 PreparedStatement ps = con.prepareStatement(sql)) {

			System.out.print("Introduce el id_usuario a eliminar (ej. 1 tiene incidencias y lanzará Error 1451): ");
			int id = Integer.parseInt(sc.nextLine().trim());
			ps.setInt(1, id);

			int filas = ps.executeUpdate();
			if (filas > 0) {
				System.out.println("[OK] Usuario eliminado correctamente.");
			} else {
				System.out.println("[AVISO] No existe ningún usuario con ID " + id + ".");
			}

		} catch (NumberFormatException e) {
			System.out.println("[AVISO] Debes introducir un ID numérico válido.");
		} catch (SQLException e) {
			gestionarErrorSQL(e);
		}
	}

	private static void actualizarRegistro(Scanner sc) {
		System.out.println("\n--- ACTUALIZACIÓN DE REGISTROS ---");
		System.out.println("1. Actualizar datos de USUARIO (email / nombre)");
		System.out.println("2. Reasignar usuario de una INCIDENCIA (prueba Error 1452)");
		System.out.print("Elige operación: ");
		String sub = sc.nextLine().trim();

		if (sub.equals("1")) {
			String sql = "UPDATE usuario SET nombre = ?, email = ? WHERE id_usuario = ?";
			try (Connection con = DriverManager.getConnection(URL, USER, PASS);
				 PreparedStatement ps = con.prepareStatement(sql)) {

				System.out.print("ID del usuario a modificar: ");
				int id = Integer.parseInt(sc.nextLine().trim());

				System.out.print("Nuevo nombre (escribe NULL para probar Error 1048): ");
				String nombre = sc.nextLine().trim();
				if (nombre.equalsIgnoreCase("NULL")) {
					ps.setNull(1, Types.VARCHAR);
				} else {
					ps.setString(1, nombre);
				}

				System.out.print("Nuevo email (sin '@' para Error 3819, o repetido para Error 1062): ");
				String email = sc.nextLine().trim();
				if (email.equalsIgnoreCase("NULL")) {
					ps.setNull(2, Types.VARCHAR);
				} else {
					ps.setString(2, email);
				}

				ps.setInt(3, id);

				int filas = ps.executeUpdate();
				if (filas > 0) {
					System.out.println("[OK] Usuario actualizado correctamente.");
				} else {
					System.out.println("[AVISO] No se encontró ningún usuario con ID " + id + ".");
				}

			} catch (NumberFormatException e) {
				System.out.println("[AVISO] El ID debe ser numérico.");
			} catch (SQLException e) {
				gestionarErrorSQL(e);
			}

		}  else if (sub.equals("2")) {
			String sql = "UPDATE incidencia SET id_usuario_reporta = ? WHERE id_incidencia = ?";
			try (Connection con = DriverManager.getConnection(URL, USER, PASS);
				 PreparedStatement ps = con.prepareStatement(sql)) {

				System.out.print("ID de la incidencia a modificar (ej. 1): ");
				int idInc = Integer.parseInt(sc.nextLine().trim());
				System.out.print("Nuevo id_usuario_reporta (pon 999 para probar Error 1452): ");
				int idUsr = Integer.parseInt(sc.nextLine().trim());

				ps.setInt(1, idUsr);
				ps.setInt(2, idInc);

				int filas = ps.executeUpdate();
				if (filas > 0) {
					System.out.println("[OK] Incidencia reasignada correctamente.");
				} else {
					System.out.println("[AVISO] No existe la incidencia con ID " + idInc + ".");
				}

			} catch (NumberFormatException e) {
				System.out.println("[AVISO] Los IDs deben ser numéricos.");
			} catch (SQLException e) {
				gestionarErrorSQL(e);
			}
		} else {
			System.out.println("[AVISO] Opción no válida.");
		}
	}

	/**
	 * Muestra por consola el contenido de 'usuario' e 'incidencia' para verificar los cambios.
	 */
	private static void mostrarDatos() {
		try (Connection con = DriverManager.getConnection(URL, USER, PASS);
			 Statement st = con.createStatement()) {

			System.out.println("\n--- TABLA USUARIO ---");
			try (ResultSet rs = st.executeQuery("SELECT id_usuario, nombre, email, rol FROM usuario")) {
				while (rs.next()) {
					System.out.printf("ID: %d | Nombre: %s | Email: %s | Rol: %s%n",
							rs.getInt("id_usuario"), rs.getString("nombre"),
							rs.getString("email"), rs.getString("rol"));
				}
			}

			System.out.println("\n--- TABLA INCIDENCIA ---");
			try (ResultSet rs = st.executeQuery("SELECT id_incidencia, titulo, categoria, id_usuario_reporta FROM incidencia")) {
				while (rs.next()) {
					System.out.printf("ID: %d | Título: %s | Categoría: %s | Usuario ID: %d%n",
							rs.getInt("id_incidencia"), rs.getString("titulo"),
							rs.getString("categoria"), rs.getInt("id_usuario_reporta"));
				}
			}

		} catch (SQLException e) {
			gestionarErrorSQL(e);
		}
	}


	private static void gestionarErrorSQL(SQLException e) {
		int codigoError = e.getErrorCode();
		String sqlState = e.getSQLState();

		System.out.println("\n[OPERACIÓN RECHAZADA POR LA BASE DE DATOS]");

		switch (codigoError) {
			case 1062:
				System.out.println("Motivo (Error 1062 / SQLState " + sqlState + "): Clave primaria o campo único duplicado. "
						+ "Ya existe un registro con ese identificador o correo electrónico.");
				break;

			case 1452:
				System.out.println("Motivo (Error 1452 / SQLState " + sqlState + "): Violación de clave foránea al insertar o actualizar. "
						+ "El registro padre al que intentas hacer referencia (id_usuario) no existe.");
				break;

			case 1451:
				System.out.println("Motivo (Error 1451 / SQLState " + sqlState + "): Violación de clave foránea al eliminar o actualizar. "
						+ "No se puede borrar ni cambiar el ID de este usuario porque tiene incidencias asociadas.");
				break;

			case 1048:
				System.out.println("Motivo (Error 1048 / SQLState " + sqlState + "): Campo obligatorio vacío. "
						+ "Has intentado guardar un valor nulo (NULL) en una columna configurada como NOT NULL.");
				break;

			case 3819:
				System.out.println("Motivo (Error 3819 / SQLState " + sqlState + "): Incumplimiento de restricción CHECK. "
						+ "El formato o rango del dato introducido no es válido (el email debe contener '@' y un punto).");
				break;

			default:
				System.out.println("Motivo (Error SQL " + codigoError + " / SQLState " + sqlState + "): " + e.getMessage());
				break;
		}
	}
}