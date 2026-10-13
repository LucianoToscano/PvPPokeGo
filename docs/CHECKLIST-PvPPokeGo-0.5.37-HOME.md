# PvPPokeGo 0.5.37 — Home mobile nativa (13/10/2026)

## Referências
- Código original privado: https://github.com/LucianoToscano/PvPPokeGo/tree/feature/home-mobile-0.5.37-20261013
- Build QA isolado: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/38467721349
- Espelho público de build: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/tree/preview/home-mobile-0.5.37-20261013
- Guia mestre: [PROJECT_CONTINUITY_GUIDE.md](PROJECT_CONTINUITY_GUIDE.md)

## Implementado no código
- [x] Nova Home Android nativa na vertical, visual azul translúcido e navegação inferior persistente.
- [x] Cabeçalho com ícone de lançamento original, nome e status real do serviço de overlay.
- [x] Perfil local com apelido editável e estatísticas calculadas pelo histórico (sem inventar ranking, vitórias ou poeira).
- [x] Três vagas da equipe lidas de `TeamSetupRepository`: sprites locais, nome, PC, HP, IVs, sexo e identificador do golpe rápido.
- [x] Resumo de PC médio, quantidade cadastrada e aptidão conforme limite de liga.
- [x] Liga Auto/Grande/Ultra/Mestra ligada a `SettingsRepository.leagueMode`.
- [x] Botões para iniciar/parar Overlay (com autorização existente), nova batalha e histórico.
- [x] Equipe abre painel manual/scanner anterior, Arena abre dados locais e Histórico possui exportações.
- [x] Ferramentas de reconhecimento, ataques, escudo, reservas e backup conduzem às funções existentes.
- [x] Configurações completas anteriores foram preservadas; motores OCR e HUD não modificados.
- [x] Versão de QA diferenciada: versionName 0.5.37, versionCode 42.
- [x] Branch de feature separada preserva integralmente a 0.5.36.

## Validação
- [ ] Aguardar resultado do workflow Android Standard debug e dos testes unitários no run 38467721349.
- [ ] Conferir o APK, a assinatura QA, os assets de Pokémon e checksum.
- [ ] Instalar e validar a Home num telefone Android real, em especial na largura estreita.
- [ ] Conferir clique dos três slots e retorno ao início após salvar.
- [ ] Conferir seleção de liga e recálculo de elegibilidade da equipe.
- [ ] Conferir iniciar/parar overlay, retorno da permissão, HUD e áreas de toque.
- [ ] Conferir reconhecimento aliado, 3 reservas, correspondência e continuidade de batalhas (regressão).
- [ ] Validar nomes dos golpes e densidade visual dos cards com dados reais; refinar após feedback.
- [ ] Avaliar inclusão do estádio fornecido pelo usuário como asset após finalização da etapa, sem interferir no motor.

## Limites e decisões
O layout visual do usuário serve como direção de arte; nenhuma contagem/rank/prêmio fictício foi conectado. Os dados mostrados são locais. Os cards compactos mostram dados conhecidos, e traços onde informações não foram cadastradas. Tela antiga de Ajustes preserva todos os controles anteriores; atalhos navegam para a área onde a função já existe. Sem contas online, não é possível implementar ranking coletivo ou recompensas verificáveis. Não alterar a branch 0.5.36 nem substituir APK sem build confirmado.
