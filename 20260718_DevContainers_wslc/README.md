# 開発環境 (Dev Container)

このプロジェクトは VSCode の Dev Containers を利用して開発環境を構築します。

## 構成

- **ベース OS**: Debian 13 (Trixie) slim版
- **言語**: nodejs, python, java, rust
- **VSCode 機能拡張**:
  - ESLint
  - Prettier

## セットアップ手順

1. VSCode Extensions で Dev Containers (ms-vscode-remote.remote-containers) をインストール
2. 安定版ではまだwslcに対応していないため、インストールした Extensions　を pre-release 版に更新する
3. User Settings から `Docker Path` を検索し、設定値を `wslc` に変更する
4. VSCode でこのプロジェクトを開く
5. コマンドパレット (Ctrl+Shift+P) から `Dev Containers: Reopen in Container` を選択
6. 初回起動時は Docker イメージのビルドとコンテナの起動が行われる
