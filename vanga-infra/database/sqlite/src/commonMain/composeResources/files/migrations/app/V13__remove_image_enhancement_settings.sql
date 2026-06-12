ALTER TABLE ImageReaderSettings
    DROP COLUMN onnx_runtime_mode;

ALTER TABLE ImageReaderSettings
    DROP COLUMN onnx_runtime_device_id;

ALTER TABLE ImageReaderSettings
    DROP COLUMN onnx_runtime_tile_size;

ALTER TABLE ImageReaderSettings
    DROP COLUMN onnx_runtime_model_path;
