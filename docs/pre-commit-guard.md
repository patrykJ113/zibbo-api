# 🔐 Environment variables

This project keeps secrets (DB password, etc.) in a `.env` file. The values are **encrypted** with **Dotenvx**, so the `.env` file is safe to have in the repo — nobody can read the secrets without the private key.

A **pre-commit guard** makes sure you never accidentally commit a `.env` with plaintext (unencrypted) secrets.

## What you need to install

### 1. Dotenvx — encrypts/decrypts the `.env`

```bash
npm install -g @dotenvx/dotenvx
```

### 2. pip — the package manager for Python

You need **pip** because the pre-commit tool is written in Python and is installed with it.

Same role as tools you may know: Node uses **npm**, Java uses **Maven/Gradle**, Python uses **pip**.

Check if you already have it:

```bash
pip --version
```

If that fails, try:

```bash
pip3 --version
```

- Installed → shows a version like `pip 24.0 from ...`
- Not installed → shows `command not found`

Install it (Pop!_OS / Ubuntu):

```bash
sudo apt install python3-pip
```

### 3. pre-commit — runs the safety check before each commit

Install the tool:

```bash
pip install pre-commit
```

Then activate it in the repo (see [One-time setup](#one-time-setup-after-cloning) below).

## One-time setup after cloning

Activate the guard in your local repo:

```bash
pre-commit install
```

**Why:** git hooks don't come with a clone. This command wires the check into your git so it runs automatically on every commit. You do this **once**.

## The private key 🔑

To decrypt the `.env` and run the app, you need the **`.env.keys`** file. It is **not** in the repo (it's gitignored on purpose). Get it from the project owner and place it in the project root.

**Why:** the private key is the one thing that must stay secret. If it leaked, all the encrypted values could be read.

## Daily use

Add or change a secret — use `dotenvx set` so it's encrypted right away:

```bash
dotenvx set DB_PASSWORD yourpassword
```

Read a value (decrypted):

```bash
dotenvx get DB_PASSWORD
```

## What happens when you commit

Every `git commit` triggers the guard:

- ✅ All values encrypted → commit goes through
- ❌ A plaintext secret found → commit blocked with:

```
  ❌ Run dotenvx encrypt first
```

If you get blocked, encrypt and commit again:

```bash
dotenvx encrypt
git add .env
git commit -m "your message"
```
