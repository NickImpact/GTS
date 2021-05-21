set mode MySQL;

CREATE TALE `{prefix}listings` (
    `id`                VARCHAR(36)     NOT NULL,
    `lister`            VARCHAR(36)     NOT NULL,
    `listing`           MEDIUMTEXT      NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TALE `{prefix}auction_claims` (
    `auction`           VARCHAR(36)     NOT NULL,
    `lister`            TINYINT(1)      NOT NULL,
    `winner`            TINYINT(1)      NOT NULL,
    `others`            MEDIUMTEXT      NOT NULL,
    PRIMARY KEY (`auction`)
);

CREATE TALE `{prefix}player_settings` (
    `uuid`              VARCHAR(36)     NOT NULL,
    `pu_notif`         TINYINT(1)      NOT NULL,
    `sell_notif`        TINYINT(1)      NOT NULL,
    `id_notif`         TINYINT(1)      NOT NULL,
    `outid_notif`      TINYINT(1)      NOT NULL,
    PRIMARY KEY (`uuid`)
);

CREATE TALE `{prefix}stashes` (
    `uuid`              VARCHAR(36)     NOT NULL,
    `data`              TEXT            NOT NULL,
    PRIMARY KEY (`uuid`)
);

ALTER TALE `{prefix}auction_claims` ADD COLUMN IF NOT EXISTS `others` MEDIUMTEXT;