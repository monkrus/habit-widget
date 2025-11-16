#!/bin/bash

# Push Habit Tracker Widget to GitHub
# Run this script to push all code to GitHub

echo "================================================"
echo "  Pushing Habit Tracker Widget to GitHub"
echo "================================================"
echo ""

cd /home/user/habit-widget

# Check if we're in the right directory
if [ ! -f "README.md" ]; then
    echo "❌ Error: Not in habit-tracker-widget directory"
    exit 1
fi

# Show what we're about to push
echo "📦 Repository: https://github.com/monkrus/habit-tracker-widget"
echo "🌿 Branch: main"
echo ""
echo "📝 Commits to push:"
git log --oneline origin/main..main 2>/dev/null || git log --oneline
echo ""
echo "📊 Files to push: $(git ls-files | wc -l)"
echo ""

# Try to push
echo "🚀 Pushing to GitHub..."
echo ""

# Try different authentication methods
if git push -u origin main 2>&1; then
    echo ""
    echo "✅ SUCCESS! Code pushed to GitHub!"
    echo ""
    echo "🔗 View at: https://github.com/monkrus/habit-tracker-widget"
    echo ""
else
    EXIT_CODE=$?
    echo ""
    echo "⚠️  Push requires authentication."
    echo ""
    echo "Choose an authentication method:"
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "Option 1: Personal Access Token (Recommended)"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "1. Create token at: https://github.com/settings/tokens"
    echo "   - Click 'Generate new token (classic)'"
    echo "   - Name: 'Habit Tracker Push'"
    echo "   - Scopes: Check 'repo'"
    echo "   - Click 'Generate token'"
    echo "   - COPY THE TOKEN (you won't see it again!)"
    echo ""
    echo "2. Run this command:"
    echo "   git push -u origin main"
    echo ""
    echo "3. When prompted:"
    echo "   Username: monkrus"
    echo "   Password: [paste your token]"
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "Option 2: GitHub CLI"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "1. Install: sudo apt install gh"
    echo "2. Login: gh auth login"
    echo "3. Push: git push -u origin main"
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "Option 3: SSH"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "1. Generate SSH key:"
    echo "   ssh-keygen -t ed25519 -C 'your_email@example.com'"
    echo ""
    echo "2. Add to GitHub:"
    echo "   cat ~/.ssh/id_ed25519.pub"
    echo "   (Copy and paste to: https://github.com/settings/keys)"
    echo ""
    echo "3. Change remote to SSH:"
    echo "   git remote set-url origin git@github.com:monkrus/habit-tracker-widget.git"
    echo ""
    echo "4. Push:"
    echo "   git push -u origin main"
    echo ""
    exit $EXIT_CODE
fi
