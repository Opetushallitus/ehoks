with date_range (start_date, end_date) as (
        values (%s, %s)  -- These are provided as parameters in the script
)
select split_part(k.kyselylinkki, '/', 5) as tunnus, k.hoks_id
from kyselylinkit k, date_range
where k.alkupvm >= start_date::date and k.alkupvm <= end_date::date
union  -- Should take care of any duplicates
select p.arvo_tunniste as tunnus, p.hoks_id
from palautteet p, date_range
where p.arvo_tunniste is not null
  and p.arvo_tunniste not like '%%dummy%%'  -- escaping percent for `psycopg`
  and p.heratepvm >= start_date::date
  and p.heratepvm <= end_date::date;
