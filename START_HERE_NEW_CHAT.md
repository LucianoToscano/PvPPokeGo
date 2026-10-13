# COMEÇAR AQUI — PLANO DE RECONSTRUÇÃO 1.0.0

**Nova prioridade do usuário (13/10/2026):** reconstruir o PvPPokeGo do início por funções pequenas, gerando e testando cada APK. Nenhum APK 1.0.x foi criado ainda.

**Checklist obrigatório:** [docs/REBUILD_MASTER_CHECKLIST_1.0.0.md](docs/REBUILD_MASTER_CHECKLIST_1.0.0.md)

**Biblioteca oficial de imagens:** https://drive.google.com/drive/folders/1zAEL_PhkrvXSyJ8twCWcQFKaybEtAyWI

**Inventário dos recursos visuais do APK:** [docs/IMAGE_ASSET_LIBRARY_0.5.37.md](docs/IMAGE_ASSET_LIBRARY_0.5.37.md)

**Branch de planejamento:** `rebuild/1.0.0-checklist-20261013` — somente documentação, não apagar 0.5.37/0.5.36.

**Primeiro APK planejado:** Home nativa Android, 2 estádios fornecidos pelo usuário, logo original, abas e três vagas vazias de equipe. Não misturar OCR, HUD e motor de previsão nessa primeira versão.

**Guia Mestre deste plano:** https://github.com/LucianoToscano/PvPPokeGo/blob/rebuild/1.0.0-checklist-20261013/docs/PROJECT_CONTINUITY_GUIDE.md

**Drive oficial:** https://drive.google.com/drive/folders/1ybogVRRnaOe0KtWjlovxS56bJOi_JewI

> Abaixo fica preservado o ponto de entrada da antiga linha de código 0.5.37, apenas como referência histórica.

---

# START HERE — PvPPokeGo

## Etapa atual: Home mobile 0.5.37 (APK QA pronto, teste físico pendente)

**Branch:** https://github.com/LucianoToscano/PvPPokeGo/tree/feature/home-mobile-0.5.37-20261013

**APK 0.5.37:** https://drive.google.com/file/d/1Owa3oOXRhUIkrOZj8Q9X5K0Qma1ip8Kk/view?usp=drivesdk

**Checklist:** [docs/CHECKLIST-PvPPokeGo-0.5.37-HOME.md](docs/CHECKLIST-PvPPokeGo-0.5.37-HOME.md)

**CI do APK:** https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/38467721349 — run SUCCESS; APK e ZIP verificados, SHA-256 0aa431a86f66d3cd6392330444fd3f307ef39378a28be2872aee6ea21360710d.

**Importante:** novo painel Android vertical e dados locais reais; versão 0.5.36 preservada; novo APK 0.5.37 é a versão de QA mais recente, ainda não testada em aparelho. Leia o Guia Mestre do mesmo branch da 0.5.37.


> **Este arquivo é apenas o ponto de entrada. A fonte canônica de continuidade mudou.**

Antes de qualquer trabalho, leia:

## Guia Mestre canônico — implementação mais recente validada em CI (0.5.37)

https://github.com/LucianoToscano/PvPPokeGo/blob/feature/home-mobile-0.5.37-20261013/docs/PROJECT_CONTINUITY_GUIDE.md

Arquivo:
`docs/PROJECT_CONTINUITY_GUIDE.md`

**Nova pasta oficial no Google Drive:** https://drive.google.com/drive/folders/1ybogVRRnaOe0KtWjlovxS56bJOi_JewI

**APK QA 0.5.36 no novo Drive:** https://drive.google.com/file/d/14wzZDCx0RMCFtH-olGYlFb9FDnpaATHu/view?usp=drivesdk

**Checklist atual:** [0.5.36](docs/CHECKLIST-PvPPokeGo-0.5.36.md)

**Links canônicos (não usar Drive antigo):** [PROJECT_LINKS](docs/PROJECT_LINKS.md)

Esse Guia Mestre contém:
- estado atual do projeto;
- regras permanentes;
- branch/versão;
- bugs prioritários;
- build/status real;
- checklist;
- PBE;
- fontes externas auditadas;
- protocolo obrigatório de documentação;
- registro cronológico;
- prompt padrão para abrir novo chat.

## Regra

Não use o conteúdo antigo deste arquivo como estado da versão atual.
Primeiro verifique o GitHub e o Guia Mestre.

**Toda mudança/correção/build/teste/entrega relevante deve ser registrada no Guia Mestre.**

Regra principal:

**MELHORAR E ORGANIZAR, NUNCA EMPOBRECER.**
