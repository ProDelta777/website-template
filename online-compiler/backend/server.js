const express = require('express');
const cors = require('cors');
const { spawn, exec } = require('child_process');
const fs = require('fs');
const path = require('path');
const { v4: uuidv4 } = require('uuid'); // Will use crypto if uuid not available, but let's just write simple random strings

const app = express();
app.use(cors());
app.use(express.json());

const tempDir = path.join(__dirname, 'temp');
if (!fs.existsSync(tempDir)) {
    fs.mkdirSync(tempDir);
}

const generateId = () => Math.random().toString(36).substring(2, 15);

const isUnsafeCode = (language, code) => {
    if (language === 'python') {
        const forbiddenPatterns = [
            /import\s+os/i,
            /import\s+subprocess/i,
            /import\s+sys/i,
            /__import__/i,
            /eval\s*\(/i,
            /exec\s*\(/i,
            /os\.system/i,
            /subprocess\./i,
            /open\s*\(/i
        ];
        return forbiddenPatterns.some(pattern => pattern.test(code));
    } else if (language === 'cpp') {
        const forbiddenPatterns = [
            /#include\s*<stdlib\.h>/i,
            /#include\s*<cstdlib>/i,
            /#include\s*<fstream>/i,
            /system\s*\(/i,
            /popen\s*\(/i,
            /exec\s*\(/i,
            /fork\s*\(/i
        ];
        return forbiddenPatterns.some(pattern => pattern.test(code));
    }
    return false;
};

app.post('/api/execute', async (req, res) => {
    const { language, code } = req.body;

    if (!language || !code) {
        return res.status(400).json({ error: 'Language and code are required.' });
    }

    if (isUnsafeCode(language, code)) {
        return res.status(403).json({ error: 'Code contains dangerous or unauthorized commands and was blocked.' });
    }

    const fileId = generateId();
    let filePath, executablePath;

    try {
        if (language === 'python') {
            filePath = path.join(tempDir, `${fileId}.py`);
            fs.writeFileSync(filePath, code);

            const start = performance.now();
            const child = spawn('python3', [filePath]);

            let output = '';
            let error = '';

            const timeout = setTimeout(() => {
                child.kill('SIGTERM');
            }, 5000);

            child.stdout.on('data', (data) => output += data.toString());
            child.stderr.on('data', (data) => error += data.toString());

            child.on('close', (code) => {
                clearTimeout(timeout);
                const end = performance.now();
                fs.unlinkSync(filePath); // cleanup

                if (code !== 0 && error) {
                     // Check if it was killed by timeout
                     if (!child.signalCode && error) {
                         return res.json({ output: '', error, executionTime: (end - start).toFixed(2) });
                     }
                }

                if (child.signalCode === 'SIGTERM') {
                    return res.json({ output: output, error: 'Execution Timed Out (5s)', executionTime: (end - start).toFixed(2) });
                }

                res.json({ output, error, executionTime: (end - start).toFixed(2) });
            });

            child.on('error', (err) => {
                res.status(500).json({ error: 'Failed to start execution process.' });
            });

        } else if (language === 'cpp') {
            filePath = path.join(tempDir, `${fileId}.cpp`);
            executablePath = path.join(tempDir, `${fileId}`);
            fs.writeFileSync(filePath, code);

            const start = performance.now();

            // Compile
            exec(`g++ ${filePath} -o ${executablePath}`, (compileError, stdout, stderr) => {
                if (compileError) {
                    fs.unlinkSync(filePath);
                    return res.json({ output: '', error: stderr || compileError.message, executionTime: 0 });
                }

                // Run
                const child = spawn(executablePath);

                let output = '';
                let error = '';

                const timeout = setTimeout(() => {
                    child.kill('SIGTERM');
                }, 5000);

                child.stdout.on('data', (data) => output += data.toString());
                child.stderr.on('data', (data) => error += data.toString());

                child.on('close', (code) => {
                    clearTimeout(timeout);
                    const end = performance.now();

                    fs.unlinkSync(filePath);
                    if (fs.existsSync(executablePath)) {
                        fs.unlinkSync(executablePath);
                    }

                    if (child.signalCode === 'SIGTERM') {
                        return res.json({ output: output, error: 'Execution Timed Out (5s)', executionTime: (end - start).toFixed(2) });
                    }

                    res.json({ output, error, executionTime: (end - start).toFixed(2) });
                });

                child.on('error', (err) => {
                    res.status(500).json({ error: 'Failed to start execution process.' });
                });
            });

        } else {
            return res.status(400).json({ error: 'Unsupported language.' });
        }
    } catch (err) {
        res.status(500).json({ error: 'Internal server error.' });
    }
});

const PORT = process.env.PORT || 5000;
app.listen(PORT, () => {
    console.log(`Backend running on port ${PORT}`);
});
