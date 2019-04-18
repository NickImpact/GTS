-- GTS MySQL Schema

CREATE TABLE `{prefix}listings_v3` (
  `uuid`      VARCHAR(36) NOT NULL,
  `owner`     VARCHAR(36) NOT NULL,
  `class`     VARCHAR(20) NOT NULL,
  `listing`   MEDIUMTEXT  NOT NULL,
  PRIMARY KEY (`uuid`)
) DEFAULT CHARSET = utf8;

CREATE TABLE `{prefix}ignorers` (
  `uuid`      VARCHAR(36) NOT NULL,
  PRIMARY KEY (`uuid`)
) DEFAULT CHARSET = utf8;