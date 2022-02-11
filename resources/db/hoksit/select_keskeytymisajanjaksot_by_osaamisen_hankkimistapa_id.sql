SELECT * FROM keskeytymisajanjaksot
  WHERE
    deleted_at IS NULL
    AND osaamisen_hankkimistapa_id = ?
