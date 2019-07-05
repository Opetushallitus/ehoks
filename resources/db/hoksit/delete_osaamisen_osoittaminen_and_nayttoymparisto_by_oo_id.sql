-- DELETE FROM nayttoymparistot WHERE id IN (SELECT nayttoymparisto_id FROM osaamisen_osoittamiset
-- WHERE id IN (SELECT osaamisen_osoittaminen_id
-- FROM aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto WHERE aiemmin_hankittu_yhteinen_tutkinnon_osa_id= ?));

DELETE FROM nayttoymparistot WHERE id IN (SELECT nayttoymparisto_id FROM osaamisen_osoittamiset
WHERE id=?) RETURNING id;
