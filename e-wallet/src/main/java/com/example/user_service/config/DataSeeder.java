package com.example.user_service.config;

import com.example.user_service.model.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class DataSeeder implements CommandLineRunner {

    @PersistenceContext
    private EntityManager em;

    private final TransactionTemplate txTemplate;

    public DataSeeder(TransactionTemplate txTemplate) {
        this.txTemplate = txTemplate;
    }

    private static final int ACC_COUNT = 10_000;
    private static final int TX_COUNT  = 100_000;
    private static final int BATCH_SIZE = 1000;

    // linked_bank_id mặc định (đã có record trong DB)
    private static final UUID DEFAULT_LINKED_BANK_ID =
            UUID.fromString("2b3c8e41-0194-466c-860c-d0ca496d18cf");

    private final SecureRandom rnd = new SecureRandom();

    @Override
    public void run(String... args) {
        txTemplate.execute(status -> {
            seedAll();
            return null;
        });
        System.out.println("✅ Seed completed");
    }

    private void seedAll() {
        // tạo reference tới LinkedBank có sẵn
        LinkedBank bankRef = em.getReference(LinkedBank.class, DEFAULT_LINKED_BANK_ID);

        // 1) seed accounts
        List<UUID> accountIds = seedAccounts(ACC_COUNT);

        // 2) seed transactions
        seedTransactions(TX_COUNT, accountIds, bankRef);
    }

    /**
     * Seed accounts và trả về list accountId (UUID) để tạo transaction.
     * Không giữ entity Account trong memory (tránh detached issues).
     */
    private List<UUID> seedAccounts(int n) {
        List<UUID> ids = new ArrayList<>(n);

        for (int i = 1; i <= n; i++) {
            Account a = new Account();

//            UUID id = UUID.randomUUID();
//            a.setId(id);

            a.setAccountNumber(String.format("AC%010d", i));
            a.setUserName("user" + i);
            a.setPassword("pass" + i); // seed thôi
            a.setFullName("User " + i);
            a.setEmail("user" + i + "@example.com");
            a.setAddress("VN-" + (i % 1000));
            a.setBalance(0L);
            a.setHeld(0L);
            a.setActive(true);
            a.setRole("USER");

            em.persist(a);
            ids.add(a.getId());

            if (i % BATCH_SIZE == 0) {
                em.flush();
                em.clear();
                System.out.println("Seeded accounts: " + i);
            }
        }

        em.flush();
        em.clear();
        return ids;
    }

    private void seedTransactions(int n, List<UUID> accountIds, LinkedBank bankRef) {
        for (int i = 1; i <= n; i++) {
            Transaction tx = new Transaction();

//            tx.setId(UUID.randomUUID());
            tx.setRequestId("REQ-" + UUID.randomUUID()); // unique
            tx.setAmount(randomAmount());
            tx.setMessage("seed-" + i);

            TypeEnum type = randomType();
            tx.setType(type);

            tx.setStatus(rnd.nextInt(100) < 95 ? StatusEnum.COMPLETED : StatusEnum.FAILED);

            // set createdAt/updatedAt nếu BaseClass của bạn KHÔNG auto
            // Nếu BaseClass auto audit rồi thì bạn có thể bỏ dòng này.
            setCreatedUpdatedIfPossible(tx);

            UUID a1 = accountIds.get(rnd.nextInt(accountIds.size()));
            UUID a2 = accountIds.get(rnd.nextInt(accountIds.size()));
            while (a2.equals(a1)) {
                a2 = accountIds.get(rnd.nextInt(accountIds.size()));
            }

            // Gán quan hệ theo type:
            // - DEPOSIT: toAccount + linkedBank
            // - WITHDRAW: fromAccount + linkedBank
            // - TRANSFER: fromAccount + toAccount (+ linkedBank tuỳ constraint)
            if (type == TypeEnum.DEPOSIT) {
                tx.setFromAccount(null);
                tx.setToAccount(em.getReference(Account.class, a1));
                tx.setLinkedBank(bankRef);

            } else if (type == TypeEnum.WITHDRAW) {
                tx.setFromAccount(em.getReference(Account.class, a1));
                tx.setToAccount(null);
                tx.setLinkedBank(bankRef);

            } else { // TRANSFER
                tx.setFromAccount(em.getReference(Account.class, a1));
                tx.setToAccount(em.getReference(Account.class, a2));

                // Nếu cột linked_bank_id NOT NULL trong DB thì set bankRef luôn cho TRANSFER
                // Nếu nullable thì để null cũng được.
                tx.setLinkedBank(null);
            }

            em.persist(tx);

            if (i % BATCH_SIZE == 0) {
                em.flush();
                em.clear();
                System.out.println("Seeded transactions: " + i);
            }
        }

        em.flush();
        em.clear();
    }

    private long randomAmount() {
        // 10_000 -> 5_000_000
        long range = 4_990_001L;
        long x = Math.abs(rnd.nextLong()) % range;
        return 10_000L + x;
    }

    private TypeEnum randomType() {
        int x = rnd.nextInt(100);
        if (x < 35) return TypeEnum.DEPOSIT;   // 35%
        return TypeEnum.TRANSFER;  // 35%
                    // 30%
    }

    private void setCreatedUpdatedIfPossible(Transaction tx) {
        try {
            Instant now = Instant.now();
            Instant t = now.minus(rnd.nextInt(90), ChronoUnit.DAYS)
                    .minus(rnd.nextInt(24), ChronoUnit.HOURS)
                    .minus(rnd.nextInt(60), ChronoUnit.MINUTES);

            // gọi qua reflection để không phụ thuộc BaseClass bạn dùng kiểu gì
            tx.getClass().getMethod("setCreatedAt", Instant.class).invoke(tx, t);
            tx.getClass().getMethod("setUpdatedAt", Instant.class).invoke(tx, t);
        } catch (Exception ignored) {
            // BaseClass auto audit hoặc field type khác -> bỏ qua
        }
    }
}

