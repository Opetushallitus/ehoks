SELECT * FROM hoksit
where id > ? AND ((cast(? as date) is null ) OR ? < updated_at)
order by id
limit ?
