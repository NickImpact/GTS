-- GTS H2 Schema

CREATE TABLE `{prefix}listings` (
  `id`        INT         NOT NULL,
  `uuid`      VARCHAR(36) NOT NULL,
  `listing`   MEDIUMTEXT  NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `{prefix}logs` (
  `id`        INT         NOT NULL,
  `uuid`      VARCHAR(36) NOT NULL,
  `log`       MEDIUMTEXT  NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `{prefix}held_entries` (
  `id`        INT         NOT NULL,
  `holder`    MEDIUMTEXT  NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `{prefix}held_prices` (
  `id`        INT         NOT NULL,
  `holder`    MEDIUMTEXT  NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `{prefix}ignorers` (
  `uuid`      VARCHAR(36) NOT NULL,
  PRIMARY KEY (`uuid`)
);