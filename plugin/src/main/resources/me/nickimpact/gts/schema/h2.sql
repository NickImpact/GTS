-- GTS H2 Schema

CREATE TABLE `{prefix}listings_v2` (
  `uuid`      VARCHAR(36) NOT NULL,
  `owner`     VARCHAR(36) NOT NULL,
  `listing`   MEDIUMTEXT  NOT NULL,
  PRIMARY KEY (`uuid`)
);

CREATE TABLE `{prefix}logs_v2` (
  `uuid`      VARCHAR(36) NOT NULL,
  `owner`     VARCHAR(36) NOT NULL,
  `log`       MEDIUMTEXT  NOT NULL,
  PRIMARY KEY (`uuid`)
);

CREATE TABLE `{prefix}held_entries_v2` (
  `uuid`      VARCHAR(36) NOT NULL,
  `holder`    MEDIUMTEXT  NOT NULL,
  PRIMARY KEY (`uuid`)
);

CREATE TABLE `{prefix}held_prices_v2` (
  `uuid`      VARCHAR(36) NOT NULL,
  `holder`    MEDIUMTEXT  NOT NULL,
  PRIMARY KEY (`uuid`)
);

CREATE TABLE `{prefix}ignorers` (
  `uuid`      VARCHAR(36) NOT NULL,
  PRIMARY KEY (`uuid`)
);