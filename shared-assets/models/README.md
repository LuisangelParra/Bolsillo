# shared-assets/models

On-device AI categorization model artifacts. **No binaries are committed yet** — this directory documents the planned artifacts and the export pipeline so both platforms consume equivalent models.

## Planned artifacts

| File | Platform | Runtime | Notes |
|---|---|---|---|
| `category-classifier.mlpackage` | iOS | Core ML | Frozen text encoder + classification head. Bundled or optional download. |
| `category-classifier.tflite` | Android | LiteRT (ex-TFLite) | CPU/GPU/NPU with **mandatory CPU fallback**. |
| `category-classifier.onnx` | Both (option) | ONNX Runtime Mobile | Single artifact alternative to twin per-platform models. |
| `text-encoder/` | Both | — | Lightweight quantized text encoder (or hashed n-grams on low-end). |

## Design constraints (from TRD §6 + constitution Art II/V)

- **100% on-device** inference and learning. No financial data leaves the device.
- Cascade: user rules → merchant dictionary (`../merchant-dictionary/`) → ML classifier. The ML layer is **only** reached when the first two miss.
- Output: `topCategoryId`, `confidence ∈ [0,1]`, top-3 alternatives. Default threshold **0.75**.
- **Personalization is local**: a corrections table re-tunes a lightweight head (kNN / logistic) — base-model updates must **not** overwrite the user's personalization.
- Inference budget **≤ 200 ms**; app + base model **≤ ~150 MB** (model becomes a download if larger).

## Export pipeline (TBD)

1. Train base classifier on the generic corpus (ES/LatAm + EN merchant terms).
2. Export to Core ML (`coremltools`) and LiteRT (`tf`/`ai-edge-torch`), or a single ONNX graph.
3. Version via `model_version` (`AppSetting`); ship in-app or as an optional download.
4. Validate accuracy + confidence calibration before bundling (see TRD §6.7, §14).

Categories referenced by the model MUST match `../taxonomy/category-taxonomy.json` IDs.
