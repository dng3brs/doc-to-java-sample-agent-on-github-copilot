# [Agent Skills] ui-ux-pro-max

`Agent Skills` `開発` `画面`

## 📋 概要
- 簡単なプロンプトでリッチなフロントエンドを生成してくれる
- URLを渡すと、それと同じような感じのUIを生成してくれる

---

## 🎬 生成させてみた例

プロンプト：
```
/ui-ux-pro-max
  フリーアドレスのオフィスにおいて、現在自分が座っている場所をオフィスレイアウト上で登録し、共有するための社内アプリを
  開発します。単一の画面として作成して。Next.jsとtailwindを使うつもりです。
```

キャプチャの左下にアイコンがある通り、Next.jsで動作し実際に触れる画面が生成されました。
<img width="1886" height="894" alt="image" src="https://github.com/user-attachments/assets/b8cfbf2a-8011-46a7-ab9e-b5c591f279b2" />

---

## 🛠️ 再現手順
### 前提環境
- **使用ツール：** Antigravity CLI（Gemini 3.5 Flash medium）　# 主要なSkillsが使えるエージェントなら何でもよいはず
- **環境：** Linux（WSL）でpythonとnodejsはインストール済

### Agent Skillsのインストール

[https://github.com/nextlevelbuilder/ui-ux-pro-max-skill](https://github.com/nextlevelbuilder/ui-ux-pro-max-skill) の `Using CLI` の手順のとおり：
- `npm install -g ui-ux-pro-max-cli` でインストール
- 空フォルダを作成してcd
- `uipro init --ai antigravity` (使用するコーディングエージェントに合わせる）

ここまでできたら、コーディングエージェントを起動して、上記例のようなプロンプトを実行する。これだけ。
