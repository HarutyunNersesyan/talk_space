-- Existing installations are migrated here; on a fresh database Hibernate creates the table.
DO $$
BEGIN
    IF to_regclass('public.chat_messages') IS NOT NULL THEN
        ALTER TABLE public.chat_messages ADD COLUMN IF NOT EXISTS edited boolean NOT NULL DEFAULT false;
        ALTER TABLE public.chat_messages ADD COLUMN IF NOT EXISTS deleted_for_sender boolean NOT NULL DEFAULT false;
        ALTER TABLE public.chat_messages ADD COLUMN IF NOT EXISTS deleted_for_receiver boolean NOT NULL DEFAULT false;
    END IF;
END $$;
