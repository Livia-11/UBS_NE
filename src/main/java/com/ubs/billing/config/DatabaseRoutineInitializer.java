package com.ubs.billing.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseRoutineInitializer {
    private final JdbcTemplate jdbcTemplate;

    public DatabaseRoutineInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @SuppressWarnings("SqlNoDataSourceInspection")
    @EventListener(ApplicationReadyEvent.class)
    public void createPostgresRoutines() {
        // PostgreSQL routines keep notification side effects close to bill/payment data changes.
        jdbcTemplate.execute("""
                CREATE OR REPLACE FUNCTION create_bill_notification()
                RETURNS TRIGGER AS $$
                DECLARE
                    customer_name TEXT;
                BEGIN
                    SELECT full_names INTO customer_name FROM customers WHERE id = NEW.customer_id;
                    INSERT INTO notifications(customer_id, bill_id, message, status, created_at)
                    VALUES (
                        NEW.customer_id,
                        NEW.id,
                        'Dear ' || customer_name || ',' || chr(10) ||
                        'Your ' || NEW.bill_month || '/' || NEW.bill_year ||
                        ' utility bill of ' || NEW.total_amount ||
                        ' FRW has been successfully processed.',
                        'CREATED',
                        NOW()
                    );
                    RETURN NEW;
                END;
                $$ LANGUAGE plpgsql;
                """);

        jdbcTemplate.execute("""
                CREATE OR REPLACE FUNCTION mark_bill_paid_when_balance_zero()
                RETURNS TRIGGER AS $$
                BEGIN
                    IF NEW.outstanding_balance <= 0 THEN
                        NEW.outstanding_balance := 0;
                        NEW.status := 'PAID';
                    END IF;
                    RETURN NEW;
                END;
                $$ LANGUAGE plpgsql;
                """);

        jdbcTemplate.execute("""
                CREATE OR REPLACE FUNCTION create_full_payment_notification()
                RETURNS TRIGGER AS $$
                DECLARE
                    customer_name TEXT;
                BEGIN
                    IF OLD.status IS DISTINCT FROM 'PAID' AND NEW.status = 'PAID' THEN
                        SELECT full_names INTO customer_name FROM customers WHERE id = NEW.customer_id;
                        INSERT INTO notifications(customer_id, bill_id, message, status, created_at)
                        VALUES (
                            NEW.customer_id,
                            NEW.id,
                            'Dear ' || customer_name || ',' || chr(10) ||
                            'Your ' || NEW.bill_month || '/' || NEW.bill_year ||
                            ' utility bill payment of ' || NEW.total_amount ||
                            ' FRW has been paid successfully.',
                            'CREATED',
                            NOW()
                        );
                    END IF;
                    RETURN NEW;
                END;
                $$ LANGUAGE plpgsql;
                """);

        jdbcTemplate.execute("DROP TRIGGER IF EXISTS trg_bill_notification ON bills");
        jdbcTemplate.execute("""
                CREATE TRIGGER trg_bill_notification
                AFTER INSERT ON bills
                FOR EACH ROW
                EXECUTE FUNCTION create_bill_notification()
                """);

        jdbcTemplate.execute("DROP TRIGGER IF EXISTS trg_bill_paid_status ON bills");
        jdbcTemplate.execute("""
                CREATE TRIGGER trg_bill_paid_status
                BEFORE UPDATE OF outstanding_balance ON bills
                FOR EACH ROW
                EXECUTE FUNCTION mark_bill_paid_when_balance_zero()
                """);

        jdbcTemplate.execute("DROP TRIGGER IF EXISTS trg_full_payment_notification ON bills");
        jdbcTemplate.execute("""
                CREATE TRIGGER trg_full_payment_notification
                AFTER UPDATE OF status ON bills
                FOR EACH ROW
                EXECUTE FUNCTION create_full_payment_notification()
                """);
    }
}
