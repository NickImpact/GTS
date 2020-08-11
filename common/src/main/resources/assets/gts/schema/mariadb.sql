CREATE TABLE `{prefix}listings` (
    `id`                VARCHAR(36)     NOT NULL,
    `lister`            VARCHAR(36)     NOT NULL,
    `entry`             CLOB            NOT NULL,
    `price`             CLOB            NOT NULL,
    `expiration`        TIMESTAMP       NOT NULL,
    PRIMARY KEY (`id`)
) DEFAULT CHARSET = utf8;

CREATE TABLE `{prefix}player_settings` (
    `uuid`              VARCHAR(36)     NOT NULL,
    `pub_notif`         TINYINT(1)      NOT NULL,
    `sell_notif`        TINYINT(1)      NOT NULL,
    `bid_notif`         TINYINT(1)      NOT NULL,
    `outbid_notif`      TINYINT(1)      NOT NULL,
    PRIMARY KEY (`uuid`)
) DEFAULT CHARSET = utf8;

CREATE TABLE `{prefix}stashes` (
    `uuid`              VARCHAR(36)     NOT NULL,
    `data`              CLOB            NOT NULL,
    PRIMARY KEY (`uuid`)
) DEFAULT CHARSET = utf8;