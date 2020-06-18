SELECT *
    FROM kyselylinkit
    WHERE oppija_oid = ?
      AND alkupvm <= now()
