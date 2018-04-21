-- GTS MySQL Schema

CREATE TABLE `{prefix}listings_v2` (
  `uuid`      VARCHAR(36) NOT NULL,
  `owner`     VARCHAR(36) NOT NULL,
  `listing`   BLOB        NOT NULL,
  PRIMARY KEY (`uuid`)
) DEFAULT CHARSET = utf8;

CREATE TABLE `{prefix}logs_v2` (
  `uuid`      VARCHAR(36) NOT NULL,
  `owner`     VARCHAR(36) NOT NULL,
  `log`       MEDIUMTEXT  NOT NULL,
  PRIMARY KEY (`uuid`)
) DEFAULT CHARSET = utf8;

CREATE TABLE `{prefix}held_entries_v2` (
  `uuid`      VARCHAR(36) NOT NULL,
  `holder`    MEDIUMTEXT  NOT NULL,
  PRIMARY KEY (`uuid`)
) DEFAULT CHARSET = utf8;

CREATE TABLE `{prefix}held_prices_v2` (
  `uuid`      VARCHAR(36) NOT NULL,
  `holder`    MEDIUMTEXT  NOT NULL,
  PRIMARY KEY (`uuid`)
) DEFAULT CHARSET = utf8;

CREATE TABLE `{prefix}ignorers` (
  `uuid`      VARCHAR(36) NOT NULL,
  PRIMARY KEY (`uuid`)
) DEFAULT CHARSET = utf8;