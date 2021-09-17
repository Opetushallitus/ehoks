SELECT * FROM hoksit
      WHERE osaamisen_saavuttamisen_pvm >= ?
      AND osaamisen_saavuttamisen_pvm <= ?
      AND deleted_at IS NULL
