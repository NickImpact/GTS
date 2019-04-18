CREATE TABLE `{prefix}listings_v3` (
  `id`          VARCHAR(36)   NOT NULL,
  `owner`       VARCHAR(36)   NOT NULL,
  `class`       VARCHAR(20)   NOT NULL,
  `entry`       MEDIUMTEXT    NOT NULL,
  `price`       NUMERIC(12,2) NOT NULL,
  `expiration`  DATE          NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `{prefix}ignorers` (
  `uuid`      VARCHAR(36) NOT NULL,
  PRIMARY KEY (`uuid`)
);