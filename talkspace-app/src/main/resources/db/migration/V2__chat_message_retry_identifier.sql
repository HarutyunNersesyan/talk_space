-- Additive, PostgreSQL-specific migration for an existing TalkSpace database.
-- Fresh installs may run Flyway before Hibernate has created the table; in that
-- case Hibernate creates the column and constraint from the entity mapping.
DO $$
BEGIN
    IF to_regclass('public.chat_messages') IS NOT NULL THEN
        ALTER TABLE public.chat_messages ADD COLUMN IF NOT EXISTS client_message_id varchar(36);
        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint
            WHERE conrelid = 'public.chat_messages'::regclass
              AND conname = 'uq_chat_sender_client_id'
        ) THEN
            ALTER TABLE public.chat_messages
                ADD CONSTRAINT uq_chat_sender_client_id UNIQUE (sender_user_id, client_message_id);
        END IF;
    END IF;
END $$;
