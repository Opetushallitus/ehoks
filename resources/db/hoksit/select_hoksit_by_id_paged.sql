SELECT * FROM hoksit
where id > ? AND (? IS NULL OR ? < updated_at)
order by id
limit ?
