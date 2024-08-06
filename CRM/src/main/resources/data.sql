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