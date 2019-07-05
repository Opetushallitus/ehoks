DELETE FROM nayttoymparistot WHERE id IN (SELECT nayttoymparisto_id FROM osaamisen_osoittamiset
WHERE id=?) RETURNING id;
