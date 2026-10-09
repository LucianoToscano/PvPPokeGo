# PvPPokeGo — Configurações do Treinador (referência visual, 2026-10-09)

**Estado:** código gravado na branch `feature/team-recognition-modes-0.5.32`, **sem compilação, testes executados ou novo APK**, conforme solicitação expressa do usuário.

## Referências obrigatórias

- Guia Mestre canônico da versão estável: https://github.com/LucianoToscano/PvPPokeGo/blob/release/0.5.29/docs/PROJECT_CONTINUITY_GUIDE.md
- Referência histórica: https://github.com/LucianoToscano/PvPPokeGo/blob/release/0.5.26/docs/PROJECT_CONTINUITY_GUIDE.md
- Continuidade da feature: [PROJECT_CONTINUITY_GUIDE.md](PROJECT_CONTINUITY_GUIDE.md).
- Dependência funcional: [TEAM_RECOGNITION_MODES_0.5.32.md](TEAM_RECOGNITION_MODES_0.5.32.md) para Automático/Manual.
- Captura do usuário: tela "Configurações", seções **Gerais, PvP IV, Gerador de Nomes, Formato da Data Pego, Simulador de Batalha**, mais os campos **Nível do Treinador (54)** e **Equipe (Mystic)**. Os valores **54/Mystic são exemplo da captura**, não são dados verificados sobre o usuário e NÃO são escritos como valores default.

## Design escolhido

- Adicionar card recolhível "CONFIGURAÇÕES DO TREINADOR" na tela inicial existente, perto de SUA EQUIPE e Arena, sem aumentar o HUD de batalha e sem duplicar a tela Configurações nativa do telefone.
- Cinco subseções recolhíveis preservam a apresentação clean. Sempre uma seção expandida por vez, sem telas de funções vazias ou ações que aparentem estar operantes.
- **Gerais:** nível opcional 1–80, equipe de treinador **Mystic/Valor/Instinct** ou **Não informada**; salvos localmente, editáveis, sem autenticação com o Pokémon GO.
- **PvP IV:** consulta dos IVs **já digitados pelo jogador** nos três Pokémon em SUA EQUIPE; sem inventar IVs não preenchidos. O rank real de IV PvP ainda requer algoritmo íntegro com espécie/forma, nível, limite da liga e dados autorizados; não chamar soma de IV de ranking.
- **Gerador de Nomes:** gerar prévia/copiar texto para clipboard local com 3 estilos: original, nome+IVs+PC, nome+PC. Somente dados válidos existentes; nenhum toque ou escrita no Pokémon GO.
- **Formato da Data Pego:** selecionar `dd/MM/yyyy`, `yyyy-MM-dd`, `dd/MM/yy`, com prévia usando **hoje como exemplo**, NÃO a data real de captura de um Pokémon. Não inventar integração de leitura de data. Preferência persistida para uso futuro com histórico ou coleção.
- **Simulador de Batalha:** **primeiro estágio implementado é um comparador de vantagens por TIPO** (PvPoke Game Master e `TypeChart` já integrados no app). Escolher um membro cadastrado da equipe e espécie adversária do Game Master offline, mostrar relação relativa de tipos. Rotular explicitamente "**prévia de tipos, não é simulação por turnos, dano, escudos ou previsão de vencedor**". Simulação completa ficará em pendências, sem resultado numérico falso de vitória.
- Assim como SUA EQUIPE, nenhum dado do perfil ou da prévia altera OCR, energia, capturas, reservas, HUD ou classificação competitiva.
- Preferências persistidas no mesmo arquivo `pvppokego_prefs` para aproveitamento do backup original (`SettingsRepository.exportBackupJson/importBackupJson`). Quatro chaves permitidas: `trainer_profile_level_v1`, `trainer_profile_faction_v1`, `trainer_pokemon_name_style_v1`, `trainer_catch_date_display_v1`.

## Mudanças de código

1. `app/src/main/java/com/lucianotoscano/pvppokego/data/TrainerTools.kt`: tipos de equipe/estilos de nomes/data, políticas puras; não presumem informações não informadas.
2. `app/src/main/java/com/lucianotoscano/pvppokego/data/TrainerPreferencesRepository.kt`: configurações do treinador no armazenamento local compartilhado.
3. `app/src/main/java/com/lucianotoscano/pvppokego/ui/TrainerSettingsPanel.kt`: painel Compose recolhível com cinco subseções, cópia de nomes via clipboard e prévia de tipos com Game Master.
4. `app/src/main/java/com/lucianotoscano/pvppokego/ui/SettingsScreen.kt`: integra o painel à tela existente sem alterar layout do HUD de batalha.
5. `app/src/main/java/com/lucianotoscano/pvppokego/data/SettingsRepository.kt`: inclui as quatro chaves no allowlist de backup/exportação.
6. `app/src/test/java/com/lucianotoscano/pvppokego/data/TrainerToolsTest.kt`: testes de política, fontes de verdade, nomes, datas, nível opcional; **arquivo criado, não executado**.

## Pendências / não alegar execução

- [ ] Rodar testes JUnit, testes de compatibilidade de backup e compilação Kotlin/Compose quando o usuário **autorizar**.
- [ ] Provar interface real no Samsung: rolagem, painel expandido, teclado e clipboard, mudança de equipe e nível, persistência/reinicialização.
- [ ] Integrar cálculo de IV rank PvP pela espécie/forma/nível, multiplicador e liga usando dados auditados. **Não prometer ranking IV real pelo simples 0–15/0–15/0–15**.
- [ ] Desenvolver simulador por turno/movesets/HP/escudos/energia/CMP e histórico com cálculos e testes reproduzíveis. O comparador de tipos NÃO equivale ao simulador completo.
- [ ] Registrar a data real de captura somente quando existir input de data ou uma fonte confiável, e ligar a opção de formato à apresentação desse campo.
- [ ] Aperfeiçoar gerador de nomes para limites do jogo, caracteres permitidos e modelos personalizados, evitando sobrescrever apelidos sem consentimento.
- [ ] Vincular nível/equipe ao perfil Arena público apenas com permissão explícita, sem assumir nível 54/Mystic da imagem.
- [ ] Testar import/export sem perder posições do HUD, seleções das ligas, equipes salvas e histórico.
- [ ] No futuro, eventualmente mover este card para uma aba própria de Configurações apenas com validação de navegação e sem remover itens atuais.

**Decisão:** implementar agora o painel e persistência; guardar como pendências os mecanismos que exigem cálculos novos. **NÃO COMPILEI OU GEREI APK nesta etapa.** A base continua sendo a feature 0.5.32 e a release 0.5.29 permanece intacta.
