# Checkpoint QA 0.5.38 ImageDB — 15/10/2026 (não definitivo)

- **APK Android:** https://drive.google.com/file/d/1ZbeORZKaidpDohUUArFBO67zBAlc7B69/view?usp=drivesdk — SHA256 `fda159820a46ce7952341583427fbaea8f3efc6f82198c15cdf87668e2b84ffc`, 81.006.425 bytes.
- **Fonte Android editável:** https://drive.google.com/file/d/1hW0oqCDHQbbszItvgRmd7oiL3_zsXBJX/view?usp=drivesdk — SHA256 `1fc8199bee91790144f15775a6189f3d90999321cb5c58a70b6d839af75a0bbe`, 163 arquivos, 6.307.248 bytes compactados.
- **Pacote combinado APK + fonte + assinaturas:** https://drive.google.com/file/d/1qR4Tz_RNUka5iesNuA-uHREXiBEhqwPz/view?usp=drivesdk
- **Backup separado dos módulos Hybrid 0.5.30:** https://drive.google.com/file/d/1i5796ptB_cqVH_8Zl0WqAGn73B2Oex-t/view?usp=drivesdk
- **CI final:** https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/38826162546 — SUCCESS (Gradle tests + APK + fonte ZIP).
- **Branch privada:** https://github.com/LucianoToscano/PvPPokeGo/tree/feature/definitive-imagedb-audit-0.5.38-20261015 ; **PR draft:** https://github.com/LucianoToscano/PvPPokeGo/pull/39
- **Relatório e pendências:** [AUDITORIA_DEFINITIVA_0.5.38_2026-10-15.md](AUDITORIA_DEFINITIVA_0.5.38_2026-10-15.md)
- **Limite:** QA de ImageDB, sem a fusão definitiva de Hybrid Sync e sem teste físico ou assinatura release permanente; manter a reconstrução planejada 1.0.0 e todos os registros históricos abaixo sem descarte.

---

# Banco de Imagens Pokémon 3D — entrega QA 15/10/2026

- **APK QA (0.5.37 ImageDB) — ZIP final no Drive:**
- **APK FINAL QA direto no Drive:** https://drive.google.com/file/d/1eGO1ALwCkcUWIeysE4oG5_gl_1-XlaE2/view?usp=drivesdk — 80.990.041 bytes.
 https://drive.google.com/file/d/1-vmbqRPp73Qp-MqpPsqxBOXxE9FBXuGm/view?usp=drivesdk
- **APK SHA-256:** `7828f7ad092e0f807f3a95c0f3536f3ed8164c927bf2951306ee47f316f9b9b6`; compilação/testes/assinatura v2 verificados: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/38809088247 .
- **Imagens WebP 1.071 (1.025 base + 46 Mega):** https://drive.google.com/file/d/1hScejMYPZrWSR4wuDiiQCVpuzaakPnZG/view?usp=drivesdk
- **Prancha de Mega:** https://drive.google.com/file/d/143xIgW1f2OFUPqOsy7fgnRSW6auz0IE0/view?usp=drivesdk
- **Código isolado:** https://github.com/LucianoToscano/PvPPokeGo/tree/feature/pokemon-image-db-1.0.0-20261015 ; **PR CI draft:** https://github.com/LucianoToscano/PvPPokeGo-APK-Build/pull/1
- **Status:** Build QA validado tecnicamente; instalação no telefone, assinatura release, redistribuição das artes e integração na reconstrução 1.0.0 pendentes. **O APK 0.5.37 anterior continua preservado.**

---

# Nova reconstrução PvPPokeGo 1.0.0 — planejamento

- **Checklist completo:** [REBUILD_MASTER_CHECKLIST_1.0.0.md](REBUILD_MASTER_CHECKLIST_1.0.0.md)
- **Branch:** https://github.com/LucianoToscano/PvPPokeGo/tree/rebuild/1.0.0-checklist-20261013
- **Escopo atual:** documentação de reconstrução, nenhum APK 1.0.x pronto.
- **Legado 0.5.37/0.5.36 preservado**, conforme links abaixo.
- **Google Drive oficial:** https://drive.google.com/drive/folders/1ybogVRRnaOe0KtWjlovxS56bJOi_JewI

---

# Desenvolvimento da Home mobile 0.5.37 — 13/10/2026

- **Código novo (privado):** https://github.com/LucianoToscano/PvPPokeGo/tree/feature/home-mobile-0.5.37-20261013
- **Checklist da etapa:** [CHECKLIST-PvPPokeGo-0.5.37-HOME.md](CHECKLIST-PvPPokeGo-0.5.37-HOME.md)
- **CI da prévia:** https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/38467721349 (SUCCESS, 13/10/2026, checksum/assinatura verificados)
- **APK QA 0.5.37:** https://drive.google.com/file/d/1Owa3oOXRhUIkrOZj8Q9X5K0Qma1ip8Kk/view?usp=drivesdk
- **ZIP QA 0.5.37:** https://drive.google.com/file/d/1TpLMjyYd3bQSmweD7_u-xxr35DiDzM4l/view?usp=drivesdk
- **SHA-256 do APK:** `0aa431a86f66d3cd6392330444fd3f307ef39378a28be2872aee6ea21360710d`
- **0.5.36:** entrega anterior preservada no Drive conforme links abaixo.

---

# PvPPokeGo — Links oficiais atualizados (13/10/2026)

> **Ponto único de links ativos.** Este arquivo registra a mudança de Google Drive. Links antigos nos registros cronológicos de versões passadas são históricos e NÃO devem ser usados como destino de novas entregas.

## Projeto e documentação

- **Google Drive oficial (novo):** https://drive.google.com/drive/folders/1ybogVRRnaOe0KtWjlovxS56bJOi_JewI
- **GitHub canônico (privado):** https://github.com/LucianoToscano/PvPPokeGo
- **Branch de desenvolvimento mais recente verificada:** `feature/battle-correspondence-0.5.36-20261009`
- **Guia Mestre canônico atualizado:** https://github.com/LucianoToscano/PvPPokeGo/blob/feature/battle-correspondence-0.5.36-20261009/docs/PROJECT_CONTINUITY_GUIDE.md
- **Checklist atual:** https://github.com/LucianoToscano/PvPPokeGo/blob/feature/battle-correspondence-0.5.36-20261009/docs/CHECKLIST-PvPPokeGo-0.5.36.md
- **Cópia do checklist no NOVO Google Drive:** https://drive.google.com/file/d/1j0ZqkUHPrOnM3g-zqi8MYt8lXjdS-sT5/view?usp=drivesdk
- **Handoff da correção de correspondência 0.5.36:** https://github.com/LucianoToscano/PvPPokeGo/blob/feature/battle-correspondence-0.5.36-20261009/docs/IMPLEMENTACAO_CORRESPONDENCIA_BATALHAS_0.5.36.md
- **Espelho histórico do Guia Mestre migrado para o Drive LucianoToscano (DOCX; não substituir GitHub):** https://docs.google.com/document/d/1CfSJ8o0r3qnoq3MSsfozYD1M5C07A1pL/edit?usp=drivesdk

## APK mais recente comprovado (Standard QA/debug)

- **Versão:** 0.5.36; `versionCode=41`.
- **APK no NOVO Google Drive:** https://drive.google.com/file/d/14wzZDCx0RMCFtH-olGYlFb9FDnpaATHu/view?usp=drivesdk
- **ZIP com APK, SHA256SUMS e verificação de assinatura no NOVO Google Drive:** https://drive.google.com/file/d/1Y_ngMw12VcEPKMvBWVynMHnp6WQTyFOa/view?usp=drivesdk
- **CI concluído com sucesso:** https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37992150961
- **Artifact GitHub:** `11645731162`, compilação pública isolada com atlas offline preservado.
- **SHA-256 do APK:** `e1ff7e2ac9e0a5f6e940f1d334bb2c7722409d7018bc3781253652a0679c1a94`.
- **Tamanho do APK:** 79.676.502 bytes.
- **Validações:** testes JVM/Android CI e build Standard PASS registrados; ZIP e APK `unzip -t` PASS nesta sessão, SHA256 conferido, assinatura v2 válida segundo relatório do CI; Game Master e atlas presentes.
- **Limite:** *QA no dispositivo Samsung e validação real do OCR e HUD ainda pendentes*. É APK QA/debug, não release com assinatura permanente. Exportar backup antes de instalar/atualizar.

## Procedimento para futuras entregas

1. Consultar `docs/PROJECT_CONTINUITY_GUIDE.md` e confirmar branch/HEAD reais.
2. Atualizar este arquivo **apenas após** comprovar links e hashes do artefato.
3. Enviar APK e ZIP ao **novo Drive acima**; não usar a antiga pasta da conta anterior.
4. Registrar no Guia Mestre e atualizar checklist. Preservar links históricos datados.
5. Nunca citar `sandbox:/mnt/data` de sessões antigas como link permanente. Usar o link do Drive/GitHub validado.

**Atenção:** a transferência dos arquivos históricos para este Drive já foi observada, mas não implica migrar todos os arquivos de releases anteriores. A pasta acima é o novo destino de entregas a partir de 13/10/2026.
