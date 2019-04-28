-- GTS MySQL Schema

CREATE TABLE `{prefix}listings_v3` (
  `id`          VARCHAR(36)   NOT NULL,
  `owner`       VARCHAR(36)   NOT NULL,
  `entry`       CLOB          NOT NULL,
  `price`       NUMERIC(12,2) NOT NULL,
  `expiration`  TIMESTAMP     NOT NULL,
  PRIMARY KEY (`id`)
) DEFAULT CHARSET = utf8;

CREATE TABLE `{prefix}ignorers` (
  `uuid`      VARCHAR(36) NOT NULL,
  PRIMARY KEY (`uuid`)
) DEFAULT CHARSET = utf8;