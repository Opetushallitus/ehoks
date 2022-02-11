SELECT * FROM muut_oppimisymparistot
  WHERE
    deleted_at IS NULL
    AND osaamisen_hankkimistapa_id = ?
