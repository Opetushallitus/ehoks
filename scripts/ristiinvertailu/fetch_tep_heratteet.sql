with palautteet_hoksit_opiskeluoikeudet as not materialized (
  select p.hoks_id,
         h.opiskeluoikeus_oid,
         h.oppija_oid,
         o.oppilaitos_oid as oppilaitos,
         p.jakson_yksiloiva_tunniste as yksiloiva_tunniste,
         p.koulutustoimija,
         p.heratepvm as jakso_loppupvm,
         p.toimipiste_oid,
         p.tutkintonimike,
         p.tutkintotunnus as tutkinto
  from palautteet p
  join hoksit h
  on (p.hoks_id = h.id and h.deleted_at is null and p.deleted_at is null)
  join opiskeluoikeudet o
  on (o."oid" = h.opiskeluoikeus_oid)
  where p.kyselytyyppi = 'tyopaikkajakson_suorittaneet'
    and p.tila in (:tilat)
    and p.heratepvm >= :alkupvm
    and p.heratepvm <= :loppupvm
)
select pho.*,
       hato.tutkinnon_osa_koodi_uri as tutkinnonosa_koodi,
       'hato' as tutkinnonosa_tyyppi,
       split_part(oh.osaamisen_hankkimistapa_koodi_uri, '_', 2) as hankkimistapa_tyyppi,
       oh.osa_aikaisuustieto as osa_aikaisuus,
       split_part(oh.oppisopimuksen_perusta_koodi_uri, '_', 2) as oppisopimuksen_perusta,
       tjk.vastuullinen_tyopaikka_ohjaaja_nimi as ohjaaja_nimi,
       tjk.vastuullinen_tyopaikka_ohjaaja_sahkoposti as ohjaaja_email,
       tjk.vastuullinen_tyopaikka_ohjaaja_puhelinnumero as ohjaaja_puhelinnumero,
       tjk.tyopaikan_nimi,
       tjk.tyopaikan_y_tunnus as tyopaikan_ytunnus
from palautteet_hoksit_opiskeluoikeudet pho
join hankittavat_ammat_tutkinnon_osat hato
on (hato.hoks_id = pho.hoks_id and hato.deleted_at is null)
join hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat hatooh
on (hato.id = hatooh.hankittava_ammat_tutkinnon_osa_id and hatooh.deleted_at is null)
join osaamisen_hankkimistavat oh
on (hatooh.osaamisen_hankkimistapa_id = oh.id and oh.deleted_at is null)
join tyopaikalla_jarjestettavat_koulutukset tjk
on (oh.tyopaikalla_jarjestettava_koulutus_id = tjk.id)
union all
select pho.*,
       null as tutkinnonosa_koodi,
       'hpto' as tutkinnonosa_tyyppi,
       split_part(oh.osaamisen_hankkimistapa_koodi_uri, '_', 2) as hankkimistapa_tyyppi,
       oh.osa_aikaisuustieto as osa_aikaisuus,
       split_part(oh.oppisopimuksen_perusta_koodi_uri, '_', 2) as oppisopimuksen_perusta,
       tjk.vastuullinen_tyopaikka_ohjaaja_nimi as ohjaaja_nimi,
       tjk.vastuullinen_tyopaikka_ohjaaja_sahkoposti as ohjaaja_email,
       tjk.vastuullinen_tyopaikka_ohjaaja_puhelinnumero as ohjaaja_puhelinnumero,
       tjk.tyopaikan_nimi,
       tjk.tyopaikan_y_tunnus as tyopaikan_ytunnus
from palautteet_hoksit_opiskeluoikeudet pho
join hankittavat_paikalliset_tutkinnon_osat hpto
on (pho.hoks_id = hpto.hoks_id and hpto.deleted_at is null)
join hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat hptooh
on (hpto.id = hptooh.hankittava_paikallinen_tutkinnon_osa_id and hptooh.deleted_at is null)
join osaamisen_hankkimistavat oh
on (hptooh.osaamisen_hankkimistapa_id = oh.id and oh.deleted_at is null)
join tyopaikalla_jarjestettavat_koulutukset tjk
on (tjk.id = oh.tyopaikalla_jarjestettava_koulutus_id)
union all
select pho.*,
       hyto.tutkinnon_osa_koodi_uri as tutkinnonosa_koodi,
       'hyto' as tutkinnonosa_tyyppi,
       split_part(oh.osaamisen_hankkimistapa_koodi_uri, '_', 2) as hankkimistapa_tyyppi,
       oh.osa_aikaisuustieto as osa_aikaisuus,
       split_part(oh.oppisopimuksen_perusta_koodi_uri, '_', 2) as oppisopimuksen_perusta,
       tjk.vastuullinen_tyopaikka_ohjaaja_nimi as ohjaaja_nimi,
       tjk.vastuullinen_tyopaikka_ohjaaja_sahkoposti as ohjaaja_email,
       tjk.vastuullinen_tyopaikka_ohjaaja_puhelinnumero as ohjaaja_puhelinnumero,
       tjk.tyopaikan_nimi,
       tjk.tyopaikan_y_tunnus as tyopaikan_ytunnus
from palautteet_hoksit_opiskeluoikeudet pho
join hankittavat_yhteiset_tutkinnon_osat hyto
on (pho.hoks_id = hyto.hoks_id and hyto.deleted_at is null)
join yhteisen_tutkinnon_osan_osa_alueet ytooa
on (hyto.id = ytooa.yhteinen_tutkinnon_osa_id and ytooa.deleted_at is null)
join yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat ytooaoh
on (ytooa.id = ytooaoh.yhteisen_tutkinnon_osan_osa_alue_id and ytooaoh.deleted_at is null)
join osaamisen_hankkimistavat oh
on (ytooaoh.osaamisen_hankkimistapa_id = oh.id and oh.deleted_at is null)
join tyopaikalla_jarjestettavat_koulutukset tjk
on (tjk.id = oh.tyopaikalla_jarjestettava_koulutus_id);
