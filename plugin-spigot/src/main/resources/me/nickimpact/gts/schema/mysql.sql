CREATE TABLE `{prefix}listings_v2` (
  `uuid`      VARCHAR(36) NOT NULL,
  `owner`     VARCHAR(36) NOT NULL,
  `listing`   MEDIUMTEXT  NOT NULL,
  PRIMARY KEY (`id`)
) DEFAULT CHARSET = utf8;

CREATE TABLE `{prefix}ignorers` (
  `uuid`      VARCHAR(36) NOT NULL,
  PRIMARY KEY (`uuid`)
) DEFAULT CHARSET = utf8;