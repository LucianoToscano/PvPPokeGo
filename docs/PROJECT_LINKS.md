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
