-- GTS H2 Schema

CREATE TABLE `{prefix}listings` (
  `id`        INT AUTO_INCREMENT  NOT NULL,
  `uuid`      VARCHAR(36)         NOT NULL,
  `listing`   MEDIUMTEXT          NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `{prefix}logs` (
  `id`        INT AUTO_INCREMENT  NOT NULL,
  `uuid`      VARCHAR(36)         NOT NULL,
  `log`       MEDIUMTEXT          NOT NULL,
  PRIMARY KEY (`id`)
);