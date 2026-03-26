create view oht_hoks_mapping as
 select	oht.id as hankkimistapa_id,
	oht.yksiloiva_tunniste,
	osa.hoks_id
 from	hankittavat_ammat_tutkinnon_osat osa
 join	hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat osaoh
	on (osa.id = osaoh.hankittava_ammat_tutkinnon_osa_id)
 join	osaamisen_hankkimistavat oht
	on (osaoh.osaamisen_hankkimistapa_id = oht.id)
 where	osa.deleted_at is null
 and	osaoh.deleted_at is null
 and	oht.deleted_at is null
union
 select	oht.id as hankkimistapa_id,
	oht.yksiloiva_tunniste,
	osa.hoks_id
 from	hankittavat_paikalliset_tutkinnon_osat osa
 join	hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat osaoh
	on (osa.id = osaoh.hankittava_paikallinen_tutkinnon_osa_id)
 join	osaamisen_hankkimistavat oht
	on (osaoh.osaamisen_hankkimistapa_id = oht.id)
 where	osa.deleted_at is null
 and	osaoh.deleted_at is null
 and	oht.deleted_at is null
union
 select	oht.id as hankkimistapa_id,
	oht.yksiloiva_tunniste,
	osa.hoks_id
 from	hankittavat_yhteiset_tutkinnon_osat osa
 join	yhteisen_tutkinnon_osan_osa_alueet alue
	on (osa.id = alue.yhteinen_tutkinnon_osa_id)
 join	yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat alueoh
	on (alue.id = alueoh.yhteisen_tutkinnon_osan_osa_alue_id)
 join	osaamisen_hankkimistavat oht
	on (alueoh.osaamisen_hankkimistapa_id = oht.id)
 where	osa.deleted_at is null
 and	alue.deleted_at is null
 and	alueoh.deleted_at is null
 and	oht.deleted_at is null;
