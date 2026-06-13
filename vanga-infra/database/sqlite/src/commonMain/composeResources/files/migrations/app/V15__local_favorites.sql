UPDATE AppSettings
SET serverUrl = '', username = ''
WHERE serverUrl = 'http://localhost:25600'
  AND username = 'admin@example.org';

CREATE TABLE IF NOT EXISTS LocalFavorites (
    server_url TEXT NOT NULL,
    owner_label TEXT NOT NULL,
    item_type TEXT NOT NULL,
    item_id TEXT NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (server_url, owner_label, item_type, item_id)
);

CREATE TABLE IF NOT EXISTS FavoriteSyncSettings (
    server_hash TEXT NOT NULL,
    user_hash TEXT NOT NULL,
    version INTEGER NOT NULL DEFAULT 1,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    webdav_url TEXT NOT NULL DEFAULT '',
    username TEXT NOT NULL DEFAULT '',
    password TEXT NOT NULL DEFAULT '',
    remote_path TEXT NOT NULL DEFAULT 'Vanga/favorites',
    last_synced_at TEXT,
    PRIMARY KEY (server_hash, user_hash)
);
