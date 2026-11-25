package io.joshuasalcedo.fx.sample;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/// Made-up records for the sample: contacts, invoices, tasks and files.
final class SampleData {

    private SampleData() {
    }

    record Contact(int id, String name, String email, String team) {
    }

    enum Status {
        PAID("Paid"), PENDING("Pending"), OVERDUE("Overdue");

        private final String label;

        Status(String label) {
            this.label = label;
        }

        String label() {
            return label;
        }
    }

    record Invoice(int id, String customer, Status status, BigDecimal amount, Instant issued) {
        Invoice withStatus(Status value) {
            return new Invoice(id, customer, value, amount, issued);
        }

        boolean paid() {
            return status == Status.PAID;
        }
    }

    record Task(int id, String title, String column) {
    }

    record FileEntry(String name, long size, boolean folder) {
    }

    private static final String[] FIRST = {"Ana", "Ben", "Cara", "Dan", "Eli", "Faye", "Gus", "Hana", "Ian", "Jo",
            "Kai", "Lea", "Max", "Nia", "Oto", "Pia", "Quin", "Rae", "Sam", "Tia"};
    private static final String[] LAST = {"Ruiz", "Cruz", "Lim", "Tan", "Reyes", "Santos", "Garcia", "Mendoza"};
    private static final String[] TEAMS = {"Sales", "Support", "Design"};

    static List<Contact> contacts() {
        List<Contact> out = new ArrayList<>();
        for (int i = 0; i < 160; i++) {
            String name = FIRST[i % FIRST.length] + " " + LAST[(i / FIRST.length + i) % LAST.length];
            String email = name.toLowerCase(Locale.ROOT).replace(' ', '.') + "@example.com";
            out.add(new Contact(i, name, email, TEAMS[i % TEAMS.length]));
        }
        out.sort(Comparator.comparing(Contact::name));
        return out;
    }

    static List<Invoice> invoices() {
        List<Invoice> out = new ArrayList<>();
        Instant now = Instant.now();
        for (int i = 0; i < 40; i++) {
            Status status = i % 5 == 0 ? Status.OVERDUE : i % 2 == 0 ? Status.PENDING : Status.PAID;
            String customer = FIRST[(i * 3) % FIRST.length] + " " + LAST[i % LAST.length];
            BigDecimal amount = BigDecimal.valueOf(120 + (i * 73) % 900).add(BigDecimal.valueOf(i % 4 * 25, 2));
            out.add(new Invoice(1001 + i, customer, status, amount, now.minus(i * 9L + 1, ChronoUnit.HOURS)));
        }
        return out;
    }

    static List<Task> tasks() {
        return List.of(
                new Task(1, "Write the 0.1.0 changelog", "todo"),
                new Task(2, "Release to Maven Central", "todo"),
                new Task(3, "Use ModernFX in the office desktop", "todo"),
                new Task(4, "Build the core components", "doing"),
                new Task(5, "Copy desktoppanefx into ui.core.mdi", "doing"),
                new Task(6, "Set up the Central Portal", "done"),
                new Task(7, "Generate the Tailwind classes", "done"));
    }
}
