-- :name insert!
-- :doc Create a new palaute message record.

INSERT INTO palaute_viestit (
	palaute_id,
	vastaanottaja,
	viestityyppi,
	tila,
	ulkoinen_tunniste)
VALUES (
	:palaute-id,
	:vastaanottaja,
	:viestityyppi,
	:tila,
	:ulkoinen-tunniste)
RETURNING id

-- :name get-by-tila-and-viestityypit!
-- :doc Fetch all messages (along with their respective palautteet) from
-- given viestityypit in given tila

SELECT	p.*,
	p.tila AS palaute_tila,
	pv.id AS viesti_id,
	pv.ulkoinen_tunniste,
	pv.viestityyppi,
	pv.tila AS viesti_tila,
	pv.vastaanottaja
FROM palaute_viestit pv
LEFT JOIN palautteet p ON (pv.palaute_id = p.id)
WHERE pv.viestityyppi in (:v*:viestityypit)
AND pv.tila = :tila
AND pv.deleted_at IS NULL
AND p.deleted_at IS NULL

-- :name update-tila!
-- :doc update tila for palaute message.

UPDATE	palaute_viestit
SET	tila = :tila,
	updated_at = now()
WHERE	id = :id
RETURNING id

