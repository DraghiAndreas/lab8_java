package lab8;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Year;
import java.util.ArrayList;
import java.util.Scanner;

class MainApp {
    static Scanner scanner = new Scanner(System.in);
    static final int VARSTA_MAXIMA = 120;

    // ---------------- citirea datelor de la tastatura ----------------

    public static int citireIntreg(String mesaj) {
        while (true) {
            System.out.print(mesaj);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Trebuie introdus un numar intreg!");
            }
        }
    }

    public static int citireVarsta() throws ExceptieVarsta {
        System.out.print("Varsta: ");
        int varsta;
        try {
            varsta = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            throw new ExceptieVarsta("Varsta trebuie sa contina doar cifre!");
        }
        if (varsta < 0)
            throw new ExceptieVarsta("Varsta nu poate fi negativa!");
        if (varsta > VARSTA_MAXIMA)
            throw new ExceptieVarsta("Varsta nu poate fi mai mare decat " + VARSTA_MAXIMA + "!");
        return varsta;
    }

    public static int citireAnExcursie(int varsta) throws ExceptieAnExcursie {
        System.out.print("Anul: ");
        int an;
        try {
            an = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            throw new ExceptieAnExcursie("Anul trebuie sa contina doar cifre!");
        }
        int anCurent = Year.now().getValue();
        int anNastere = anCurent - varsta;
        if (an < anNastere)
            throw new ExceptieAnExcursie("Anul excursiei nu poate fi mai mic decat anul nasterii (" + anNastere + ")!");
        if (an > anCurent)
            throw new ExceptieAnExcursie("Anul excursiei nu poate fi mai mare decat anul curent (" + anCurent + ")!");
        return an;
    }

    public static int varstaPersoana(Connection connection, int id) {
        String sql = "select varsta from persoane where id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getInt("varsta");
            }
        } catch (SQLException e) {
            System.out.println(sql);
            e.printStackTrace();
        }
        return -1;
    }

    //1. Adaugarea unei persoane
    public static void adaugarePersoana(Connection connection) {
        System.out.print("Nume: ");
        String nume = scanner.nextLine();

        int varsta;
        while (true) {
            try {
                varsta = citireVarsta();
                break;
            } catch (ExceptieVarsta e) {
                System.out.println(e.getMessage());
            }
        }

        String sql = "insert into persoane (nume, varsta) values (?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nume);
            ps.setInt(2, varsta);
            int nr_randuri = ps.executeUpdate();
            System.out.println("Numar randuri afectate de adaugare=" + nr_randuri);
        } catch (SQLException e) {
            System.out.println(sql);
            e.printStackTrace();
        }
    }

    //2. Adaugarea unei excursii
    public static void adaugareExcursie(Connection connection) {
        int id_persoana = citireIntreg("Id-ul persoanei: ");
        int varsta = varstaPersoana(connection, id_persoana);
        if (varsta == -1) {
            System.out.println("Persoana cu id-ul " + id_persoana + " nu exista in tabela persoane!");
            return;
        }

        System.out.print("Destinatia: ");
        String destinatia = scanner.nextLine();

        int anul;
        while (true) {
            try {
                anul = citireAnExcursie(varsta);
                break;
            } catch (ExceptieAnExcursie e) {
                System.out.println(e.getMessage());
            }
        }

        String sql = "insert into excursii (id_persoana, destinatia, anul) values (?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id_persoana);
            ps.setString(2, destinatia);
            ps.setInt(3, anul);
            int nr_randuri = ps.executeUpdate();
            System.out.println("Numar randuri afectate de adaugare=" + nr_randuri);
        } catch (SQLException e) {
            System.out.println(sql);
            e.printStackTrace();
        }
    }

    // 3. Afisare persoane/excusrii
    public static void afisarePersoaneSiExcursii(Connection connection) {
        String sql = "select * from persoane";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                System.out.println("id=" + rs.getInt("id") + ", nume=" + rs.getString("nume")
                        + ", varsta=" + rs.getInt("varsta"));
                afisareExcursiiPersoana(connection, rs.getInt("id"));
            }
        } catch (SQLException e) {
            System.out.println(sql);
            e.printStackTrace();
        }
    }

    public static void afisareExcursiiPersoana(Connection connection, int id_persoana) {
        String sql = "select * from excursii where id_persoana=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id_persoana);
            try (ResultSet rs = ps.executeQuery()) {
                boolean are_excursii = false;
                while (rs.next()) {
                    are_excursii = true;
                    System.out.println("    id_excursie=" + rs.getInt("id_excursie")
                            + ", destinatia=" + rs.getString("destinatia")
                            + ", anul=" + rs.getInt("anul"));
                }
                if (!are_excursii)
                    System.out.println("    nu are excursii");
            }
        } catch (SQLException e) {
            System.out.println(sql);
            e.printStackTrace();
        }
    }

    // Excursiile unei persoane (dupa nume)

    public static void excursiiDupaNume(Connection connection) {
        System.out.print("Numele persoanei: ");
        String nume = scanner.nextLine();

        String sql = "select e.id_excursie, e.destinatia, e.anul from persoane p, excursii e "
                + "where p.id=e.id_persoana and p.nume=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nume);
            try (ResultSet rs = ps.executeQuery()) {
                boolean gasit = false;
                while (rs.next()) {
                    gasit = true;
                    System.out.println("id_excursie=" + rs.getInt(1) + ", destinatia=" + rs.getString(2)
                            + ", anul=" + rs.getInt(3));
                }
                if (!gasit)
                    System.out.println("Nu s-au gasit excursii pentru persoana " + nume);
            }
        } catch (SQLException e) {
            System.out.println(sql);
            e.printStackTrace();
        }
    }

    public static void excursiiDupaNumeSeparat(Connection connection) {
        System.out.print("Numele persoanei: ");
        String nume = scanner.nextLine();

        ArrayList<Integer> id_persoane = new ArrayList<>();
        String sql1 = "select * from persoane";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql1)) {
            while (rs.next()) {
                if (rs.getString("nume").equals(nume))
                    id_persoane.add(rs.getInt("id"));
            }
        } catch (SQLException e) {
            System.out.println(sql1);
            e.printStackTrace();
            return;
        }

        if (id_persoane.isEmpty()) {
            System.out.println("Nu exista nicio persoana cu numele " + nume);
            return;
        }

        String sql2 = "select * from excursii where id_persoana=?";
        try (PreparedStatement ps = connection.prepareStatement(sql2)) {
            for (int id : id_persoane) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    boolean are_excursii = false;
                    while (rs.next()) {
                        are_excursii = true;
                        System.out.println("Persoana " + id + " - id_excursie=" + rs.getInt("id_excursie")
                                + ", destinatia=" + rs.getString("destinatia")
                                + ", anul=" + rs.getInt("anul"));
                    }
                    if (!are_excursii)
                        System.out.println("Persoana " + id + " nu are excursii");
                }
            }
        } catch (SQLException e) {
            System.out.println(sql2);
            e.printStackTrace();
        }
    }

    // 5. Persoanele care au vizitat o destinatie
    public static void persoaneDupaDestinatie(Connection connection) {
        System.out.print("Destinatia: ");
        String destinatia = scanner.nextLine();

        String sql = "select distinct p.id, p.nume, p.varsta from persoane p, excursii e "
                + "where p.id=e.id_persoana and e.destinatia=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, destinatia);
            afisarePersoane(ps);
        } catch (SQLException e) {
            System.out.println(sql);
            e.printStackTrace();
        }
    }

    // 6. Persoanele care au facut excursii intr un an
    public static void persoaneDupaAn(Connection connection) {
        int anul = citireIntreg("Anul: ");

        String sql = "select distinct p.id, p.nume, p.varsta from persoane p, excursii e "
                + "where p.id=e.id_persoana and e.anul=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, anul);
            afisarePersoane(ps);
        } catch (SQLException e) {
            System.out.println(sql);
            e.printStackTrace();
        }
    }

    public static void afisarePersoane(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            boolean gasit = false;
            while (rs.next()) {
                gasit = true;
                System.out.println("id=" + rs.getInt(1) + ", nume=" + rs.getString(2)
                        + ", varsta=" + rs.getInt(3));
            }
            if (!gasit)
                System.out.println("Nu s-au gasit persoane");
        }
    }

    // 7. Stergerea unei excursii
    public static void stergereExcursie(Connection connection) {
        int id_excursie = citireIntreg("Id-ul excursiei: ");

        String sql = "delete from excursii where id_excursie=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id_excursie);
            int nr_randuri = ps.executeUpdate();
            System.out.println("Numar randuri afectate de stergere=" + nr_randuri);
        } catch (SQLException e) {
            System.out.println(sql);
            e.printStackTrace();
        }
    }

    //8. Stergerea unei persoane si a excursiilor aferente (Cascade delete)
    public static void stergerePersoana(Connection connection) {
        int id = citireIntreg("Id-ul persoanei: ");

        // excursiile persoanei se sterg automat (ON DELETE CASCADE)
        String sql = "delete from persoane where id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            int nr_randuri = ps.executeUpdate();
            if (nr_randuri > 0)
                System.out.println("Persoana a fost stearsa impreuna cu excursiile ei");
            else
                System.out.println("Nu se gaseste nici o persoana cu id-ul specificat");
        } catch (SQLException e) {
            System.out.println(sql);
            e.printStackTrace();
        }
    }

    public static void afisareMeniu() {
        System.out.println("\n---------- MENIU ----------");
        System.out.println("1. Adaugare persoana");
        System.out.println("2. Adaugare excursie");
        System.out.println("3. Afisarea persoanelor si a excursiilor lor");
        System.out.println("4. Afisarea excursiilor unei persoane");
        System.out.println("5. Afisarea persoanelor care au vizitat o destinatie");
        System.out.println("6. Afisarea persoanelor care au facut excursii intr-un an");
        System.out.println("7. Stergerea unei excursii");
        System.out.println("8. Stergerea unei persoane");
        System.out.println("0. Iesire");
    }

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/lab8";

        try (Connection connection = DriverManager.getConnection(url, "root", "root")) {
            int optiune;
            do {
                afisareMeniu();
                optiune = citireIntreg("Optiunea: ");
                switch (optiune) {
                    case 1: adaugarePersoana(connection); break;
                    case 2: adaugareExcursie(connection); break;
                    case 3: afisarePersoaneSiExcursii(connection); break;
                    case 4: excursiiDupaNumeSeparat(connection); break;
                    case 5: persoaneDupaDestinatie(connection); break;
                    case 6: persoaneDupaAn(connection); break;
                    case 7: stergereExcursie(connection); break;
                    case 8: stergerePersoana(connection); break;
                    case 0: System.out.println("La revedere!"); break;
                    default: System.out.println("Optiune invalida!");
                }
            } while (optiune != 0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
