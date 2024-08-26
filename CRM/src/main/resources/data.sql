-- Mario Rossi
DO '
BEGIN
    IF (SELECT count(*) FROM contact WHERE user_id = ''f22bc614-4f89-4f72-a149-fdc33135b591'') = 0 THEN
        INSERT INTO contact(contact_id, name, surname, user_id, category)
        VALUES (nextval(''contact_seq''), ''Mario'', ''Rossi'', ''f22bc614-4f89-4f72-a149-fdc33135b591'', 2);

        INSERT INTO address(id, channel)
        VALUES (nextval(''address_seq''), ''email'');

        INSERT INTO email(id, email)
        VALUES (currval(''address_seq''), ''mario.rossi@example.org'');

        INSERT INTO contact_addresses(contacts_contact_id, addresses_id)
        VALUES (currval(''contact_seq''), currval(''address_seq''));
    END IF;
END;
';

-- Luigi Verdi
DO '
BEGIN
    IF (SELECT count(*) FROM contact WHERE user_id = ''12823af9-f79a-435d-b38f-2ba4c396003b'') = 0 THEN
        INSERT INTO contact(contact_id, name, surname, user_id, category)
        VALUES (nextval(''contact_seq''), ''Luigi'', ''Verdi'', ''12823af9-f79a-435d-b38f-2ba4c396003b'', 2);

        INSERT INTO address(id, channel)
        VALUES (nextval(''address_seq''), ''email'');

        INSERT INTO email(id, email)
        VALUES (currval(''address_seq''), ''luigi.verdi@example.org'');

        INSERT INTO contact_addresses(contacts_contact_id, addresses_id)
        VALUES (currval(''contact_seq''), currval(''address_seq''));
    END IF;
END;
';

-- Matteo Bianchi
DO '
BEGIN
    IF (SELECT count(*) FROM contact WHERE user_id = ''7745443f-bb2e-4d8b-8c82-e0f4ed3d17e2'') = 0 THEN
        INSERT INTO contact(contact_id, name, surname, user_id, category)
        VALUES (nextval(''contact_seq''), ''Matteo'', ''Bianchi'', ''7745443f-bb2e-4d8b-8c82-e0f4ed3d17e2'', 2);

        INSERT INTO address(id, channel)
        VALUES (nextval(''address_seq''), ''email'');

        INSERT INTO email(id, email)
        VALUES (currval(''address_seq''), ''matteo.bianchi@example.org'');

        INSERT INTO contact_addresses(contacts_contact_id, addresses_id)
        VALUES (currval(''contact_seq''), currval(''address_seq''));
    END IF;
END;
';
-- Luca Rossi
DO '
BEGIN
    IF (SELECT count(*) FROM contact WHERE user_id = ''a8e0a84c-a3b5-4965-9cac-cdb0cdfacd9b'') = 0 THEN
        INSERT INTO contact(contact_id, name, surname, user_id, category)
        VALUES (nextval(''contact_seq''), ''Luca'', ''Rossi'', ''a8e0a84c-a3b5-4965-9cac-cdb0cdfacd9b'', 1);

        INSERT INTO address(id, channel)
        VALUES (nextval(''address_seq''), ''email'');

        INSERT INTO email(id, email)
        VALUES (currval(''address_seq''), ''luca.rossi@example.org'');

        INSERT INTO contact_addresses(contacts_contact_id, addresses_id)
        VALUES (currval(''contact_seq''), currval(''address_seq''));

        INSERT INTO professional(professional_id, contact_info_contact_id, daily_rate, employment_state, location)
        VALUES (nextval(''professional_seq''), currval(''contact_seq''), 0, 0, ''Torino'');

        INSERT INTO professional_skills(professional_professional_id, skills)
        VALUES (currval(''professional_seq''), ''Proficient in Java'');

        INSERT INTO professional_skills(professional_professional_id, skills)
        VALUES (currval(''professional_seq''), ''Work in Team'');






    END IF;
END;
';
-- Giovanni Mariani
DO '
BEGIN
    IF (SELECT count(*) FROM contact WHERE user_id = ''64ee56f9-d14b-4b94-9f6a-9d2a20cb5d18'') = 0 THEN
        INSERT INTO contact(contact_id, name, surname, user_id, category)
        VALUES (nextval(''contact_seq''), ''Giovanni'', ''Mariani'', ''64ee56f9-d14b-4b94-9f6a-9d2a20cb5d18'', 1);

        INSERT INTO address(id, channel)
        VALUES (nextval(''address_seq''), ''email'');

        INSERT INTO email(id, email)
        VALUES (currval(''address_seq''), ''giovanni.mariani@example.org'');

        INSERT INTO contact_addresses(contacts_contact_id, addresses_id)
        VALUES (currval(''contact_seq''), currval(''address_seq''));

        INSERT INTO professional(professional_id, contact_info_contact_id, daily_rate, employment_state, location)
        VALUES (nextval(''professional_seq''), currval(''contact_seq''), 0, 0, ''Milano'');

        INSERT INTO professional_skills(professional_professional_id, skills)
        VALUES (currval(''professional_seq''), ''Proficient in Kotlin'');

        INSERT INTO professional_skills(professional_professional_id, skills)
        VALUES (currval(''professional_seq''), ''Work in Team'');

        INSERT INTO professional_skills(professional_professional_id, skills)
        VALUES (currval(''professional_seq''), ''Mobile Application'');
    END IF;
END;
';
-- Silvio Pellico
DO '
BEGIN
    IF (SELECT count(*) FROM contact WHERE user_id = ''631e0f6f-8f06-4007-af3c-5a823c3aae52'') = 0 THEN
        INSERT INTO contact(contact_id, name, surname, user_id, category)
        VALUES (nextval(''contact_seq''), ''Silvio'', ''Pellico'', ''631e0f6f-8f06-4007-af3c-5a823c3aae52'', 2);

        INSERT INTO address(id, channel)
        VALUES (nextval(''address_seq''), ''email'');

        INSERT INTO email(id, email)
        VALUES (currval(''address_seq''), ''silvio.pellico@example.org'');

        INSERT INTO contact_addresses(contacts_contact_id, addresses_id)
        VALUES (currval(''contact_seq''), currval(''address_seq''));
    END IF;
END;
';
-- Company A
DO '
BEGIN
    IF (SELECT count(*) FROM contact WHERE user_id = ''1f1c5579-1d62-47fd-9676-c4f8cf9d5593'') = 0 THEN
        INSERT INTO contact(contact_id, name, surname, user_id, category)
        VALUES (nextval(''contact_seq''), ''Company'', ''A'', ''1f1c5579-1d62-47fd-9676-c4f8cf9d5593'', 0);

        INSERT INTO address(id, channel)
        VALUES (nextval(''address_seq''), ''email'');

        INSERT INTO email(id, email)
        VALUES (currval(''address_seq''), ''company.a@example.org'');

        INSERT INTO contact_addresses(contacts_contact_id, addresses_id)
        VALUES (currval(''contact_seq''), currval(''address_seq''));

        INSERT INTO customer(customer_id, contact_info_contact_id)
        VALUES (nextval(''customer_seq''), currval(''contact_seq''));
    END IF;
END;
';