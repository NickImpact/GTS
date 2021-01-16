set mode MySQL;

CREATE TABLE `{prefix}listings` (
    `id`                VARCHAR(36)     NOT NULL,
    `lister`            VARCHAR(36)     NOT NULL,
    `listing`           MEDIUMTEXT      NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `{prefix}auction_claims` (
    `auction`           VARCHAR(36)     NOT NULL,
    `lister`            TINYINT(1)      NOT NULL,
    `winner`            TINYINT(1)      NOT NULL,
    `others`            MEDIUMTEXT      NOT NULL,
    PRIMARY KEY (`auction`)
);

CREATE TABLE `{prefix}player_settings` (
    `uuid`              VARCHAR(36)     NOT NULL,
    `pub_notif`         TINYINT(1)      NOT NULL,
    `sell_notif`        TINYINT(1)      NOT NULL,
    `bid_notif`         TINYINT(1)      NOT NULL,
    `outbid_notif`      TINYINT(1)      NOT NULL,
    PRIMARY KEY (`uuid`)
);

CREATE TABLE `{prefix}stashes` (
    `uuid`              VARCHAR(36)     NOT NULL,
    `data`              TEXT            NOT NULL,
    PRIMARY KEY (`uuid`)
);

ALTER TABLE `{prefix}auction_claims` ADD COLUMN IF NOT EXISTS `others` MEDIUMTEXT;