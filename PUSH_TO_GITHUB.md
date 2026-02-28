# Push to GitHub - Authentication Guide

## Current Status
✅ Repository created: https://github.com/hemantmangwani/atozshop
✅ Local repository initialized
✅ Initial commit created
✅ Remote origin configured
❌ Need authentication to push

## Issue
Your SSH key is associated with your work account (`Hemant-Mangwani_ukg`) but the repository is under your personal account (`hemantmangwani`).

---

## Solution: Use Personal Access Token

### Step 1: Create Personal Access Token

1. Go to: **https://github.com/settings/tokens**
2. Click **"Generate new token (classic)"**
3. Fill in:
   - **Note**: `AtoZShop Development`
   - **Expiration**: 90 days (or No expiration)
   - **Scopes**: Check **`repo`** (full control)
4. Click **"Generate token"** (green button at bottom)
5. **COPY THE TOKEN** (starts with `ghp_...`)
   - ⚠️ You won't see it again!
   - Save it somewhere safe

### Step 2: Push to GitHub

```bash
# Push with authentication
git push -u origin main
```

When prompted:
- **Username**: `hemantmangwani`
- **Password**: Paste your token (ghp_...)

### Step 3: Store Credentials (Optional)

To avoid entering token every time:

**macOS:**
```bash
git config --global credential.helper osxkeychain
```

**Windows:**
```bash
git config --global credential.helper wincred
```

**Linux:**
```bash
git config --global credential.helper cache
```

Then push again - credentials will be saved.

---

## Alternative: Add Personal SSH Key

If you prefer SSH (more secure):

### Step 1: Generate New SSH Key for Personal Account

```bash
# Generate new key
ssh-keygen -t ed25519 -C "your-personal-email@example.com" -f ~/.ssh/id_ed25519_personal

# Start ssh-agent
eval "$(ssh-agent -s)"

# Add key to ssh-agent
ssh-add ~/.ssh/id_ed25519_personal

# Copy public key
cat ~/.ssh/id_ed25519_personal.pub
```

### Step 2: Add SSH Key to GitHub Personal Account

1. Copy the output from the `cat` command
2. Go to: **https://github.com/settings/ssh/new**
3. **Title**: `AtoZShop Mac`
4. **Key**: Paste the public key
5. Click **"Add SSH key"**

### Step 3: Configure SSH for Multiple Accounts

Edit SSH config:
```bash
nano ~/.ssh/config
```

Add:
```
# Personal GitHub
Host github.com-personal
  HostName github.com
  User git
  IdentityFile ~/.ssh/id_ed25519_personal

# Work GitHub
Host github.com-work
  HostName github.com
  User git
  IdentityFile ~/.ssh/id_ed25519
```

### Step 4: Update Remote and Push

```bash
# Update remote to use personal SSH
git remote set-url origin git@github.com-personal:hemantmangwani/atozshop.git

# Push
git push -u origin main
```

---

## Quick Test

After setting up, test with:

```bash
# Check remote
git remote -v

# Check authentication
git fetch origin

# If successful, push
git push -u origin main
```

---

## Verify Upload

After successful push, visit:
**https://github.com/hemantmangwani/atozshop**

You should see:
- ✅ README.md (with nice formatting)
- ✅ All documentation files
- ✅ .gitignore
- ✅ src/ directory
- ✅ Initial commit message

---

## Next Steps After Push

1. **Add Repository Description**:
   - Click "About" gear icon (top right)
   - Description: "Comprehensive shop management system with POS, inventory, e-commerce, and analytics"
   - Topics: `java` `spring-boot` `postgresql` `retail` `pos` `inventory-management` `ecommerce`

2. **Enable Issues**:
   - Settings → Features → Check "Issues"

3. **Protect main branch** (optional):
   - Settings → Branches → Add rule
   - Branch name: `main`
   - Check "Require pull request before merging"

4. **Share with team**:
   - Repository URL: https://github.com/hemantmangwani/atozshop
   - Clone command: `git clone git@github.com:hemantmangwani/atozshop.git`

---

## Common Issues

### Issue: "remote: Invalid username or password"
**Solution**: You're using old password. Use Personal Access Token instead.

### Issue: "fatal: Authentication failed"
**Solution**:
1. Clear credentials: `git credential reject <<< "protocol=https\nhost=github.com\n"`
2. Try again with correct token

### Issue: "fatal: Could not read from remote repository"
**Solution**: Check repository exists and you have access.

---

## Need Help?

Run these diagnostic commands:
```bash
# Check git config
git config --list | grep -i user

# Check remote
git remote -v

# Check branch
git branch -a

# Check status
git status
```

---

**Choose Option 1 (Personal Access Token) - it's the fastest!** 🚀

After pushing, your code will be live at: https://github.com/hemantmangwani/atozshop