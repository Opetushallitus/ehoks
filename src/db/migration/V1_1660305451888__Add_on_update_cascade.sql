alter table opiskeluoikeudet
  drop constraint opiskeluoikeudet_oppija_oid_fkey,
  add constraint opiskeluoikeudet_oppija_oid_fkey
    foreign key (oppija_oid) references oppijat(oid)
      on update cascade;
