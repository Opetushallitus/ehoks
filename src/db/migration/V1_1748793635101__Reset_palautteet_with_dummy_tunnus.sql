update palautteet
set tila = 'odottaa_kasittelya', arvo_tunniste = null
where deleted_at is null and arvo_tunniste like '<dummy-%>';
