"use client";

import { useState, useRef, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { Play, Code2, Terminal, Loader2 } from "lucide-react";

type Language = "python" | "cpp";

export default function Home() {
  const [language, setLanguage] = useState<Language>("python");
  const [code, setCode] = useState({
    python: 'print("Welcome to the Cyberpunk Compiler!")\nfor i in range(5):\n    print(f"Executing sequence {i}...")',
    cpp: '#include <iostream>\n\nint main() {\n    std::cout << "Cyberpunk C++ Initialized!" << std::endl;\n    return 0;\n}'
  });
  const [output, setOutput] = useState("");
  const [error, setError] = useState("");
  const [execTime, setExecTime] = useState<string | null>(null);
  const [isRunning, setIsRunning] = useState(false);
  const [particles, setParticles] = useState<{ id: number; x: number; val: string }[]>([]);

  const handleRun = async () => {
    setIsRunning(true);
    setOutput("");
    setError("");
    setExecTime(null);
    generateParticles();

    try {
      const res = await fetch("http://localhost:5000/api/execute", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          language,
          code: code[language],
        }),
      });
      const data = await res.json();
      setOutput(data.output);
      setError(data.error);
      setExecTime(data.executionTime);
    } catch (err) {
      setError("Failed to connect to the execution server.");
    } finally {
      setIsRunning(false);
    }
  };

  const generateParticles = () => {
    const newParticles = Array.from({ length: 10 }).map((_, i) => ({
      id: Date.now() + i,
      x: Math.random() * 100,
      val: Math.random() > 0.5 ? "0" : "1",
    }));
    setParticles(newParticles);
    setTimeout(() => setParticles([]), 2000);
  };

  return (
    <main className="min-h-screen p-4 md:p-8 flex flex-col gap-6 max-w-7xl mx-auto">
      {/* Header */}
      <header className="flex justify-between items-center">
        <div className="flex items-center gap-3">
          <Code2 className="w-8 h-8 text-neon-cyan glow-text" />
          <h1 className="text-3xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-neon-cyan to-neon-purple tracking-widest uppercase">
            CyberForge
          </h1>
        </div>

        {/* Language Toggle */}
        <div className="flex gap-2 bg-black/50 p-1 rounded-lg border border-white/10 glass-panel">
          {(["python", "cpp"] as Language[]).map((lang) => (
            <button
              key={lang}
              onClick={() => setLanguage(lang)}
              className={`relative px-6 py-2 rounded-md font-bold uppercase tracking-wider text-sm transition-colors ${
                language === lang ? "text-white" : "text-gray-500 hover:text-gray-300"
              }`}
            >
              {language === lang && (
                <motion.div
                  layoutId="active-lang"
                  className="absolute inset-0 bg-gradient-to-r from-neon-purple/40 to-neon-cyan/40 rounded-md border border-neon-cyan/50 shadow-[0_0_15px_rgba(0,255,255,0.3)]"
                  transition={{ type: "spring", bounce: 0.2, duration: 0.6 }}
                />
              )}
              <span className="relative z-10">{lang === "cpp" ? "C++" : lang}</span>
            </button>
          ))}
        </div>
      </header>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 flex-1 h-[80vh]">
        {/* Editor Area */}
        <motion.div
          className="flex flex-col gap-4"
          layout
        >
          <div className="flex-1 glass-panel neon-border rounded-xl flex flex-col overflow-hidden group">
            <div className="bg-black/40 p-3 border-b border-white/10 flex justify-between items-center">
              <span className="text-xs uppercase tracking-widest text-neon-cyan">Source.sys</span>
              <motion.button
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.95 }}
                onClick={handleRun}
                disabled={isRunning}
                className="relative overflow-hidden bg-gradient-to-r from-neon-purple to-neon-cyan text-black px-6 py-1.5 rounded font-bold flex items-center gap-2 hover:shadow-[0_0_20px_rgba(0,255,255,0.5)] transition-shadow disabled:opacity-50"
              >
                {isRunning ? <Loader2 className="w-4 h-4 animate-spin" /> : <Play className="w-4 h-4 fill-black" />}
                {isRunning ? "EXECUTING..." : "COMPILE & RUN"}

                {/* Floating particles effect container */}
                <AnimatePresence>
                  {particles.map((p) => (
                    <motion.span
                      key={p.id}
                      initial={{ opacity: 1, y: 0, x: `${p.x}%` }}
                      animate={{ opacity: 0, y: -50 }}
                      exit={{ opacity: 0 }}
                      transition={{ duration: 1.5, ease: "easeOut" }}
                      className="absolute text-[10px] font-mono text-white/70"
                    >
                      {p.val}
                    </motion.span>
                  ))}
                </AnimatePresence>
              </motion.button>
            </div>
            <div className="flex-1 relative overflow-hidden">
              <AnimatePresence mode="wait">
                <motion.div
                  key={language}
                  initial={{ opacity: 0, y: 20, rotateX: 90 }}
                  animate={{ opacity: 1, y: 0, rotateX: 0 }}
                  exit={{ opacity: 0, y: -20, rotateX: -90 }}
                  transition={{ duration: 0.4 }}
                  className="absolute inset-0"
                >
                  <textarea
                    spellCheck="false"
                    value={code[language]}
                    onChange={(e) => setCode(prev => ({ ...prev, [language]: e.target.value }))}
                    className="w-full h-full bg-transparent text-gray-300 p-4 font-mono text-sm resize-none focus:outline-none focus:ring-1 focus:ring-neon-cyan/50"
                  />
                </motion.div>
              </AnimatePresence>
            </div>
          </div>
        </motion.div>

        {/* Terminal Area */}
        <motion.div
          className="flex flex-col gap-4"
          layout
        >
          <div className="flex-1 glass-panel neon-border rounded-xl flex flex-col overflow-hidden" style={{ '--color-neon-cyan': 'var(--color-neon-emerald)' } as any}>
            <div className="bg-black/40 p-3 border-b border-white/10 flex justify-between items-center">
              <div className="flex items-center gap-2 text-neon-emerald">
                <Terminal className="w-4 h-4" />
                <span className="text-xs uppercase tracking-widest">Terminal_Output</span>
              </div>
              {execTime && (
                <motion.span
                  initial={{ opacity: 0, x: 10 }}
                  animate={{ opacity: 1, x: 0 }}
                  className="text-xs font-mono text-neon-cyan border border-neon-cyan/30 px-2 py-1 rounded bg-neon-cyan/10"
                >
                  {execTime}ms
                </motion.span>
              )}
            </div>
            <div className="flex-1 p-4 font-mono text-sm overflow-auto">
              {isRunning ? (
                <div className="flex items-center gap-2 text-gray-500">
                  <span className="animate-pulse">_</span>
                  Compiling and executing sequence...
                </div>
              ) : (
                <>
                  {error && (
                    <motion.div
                      initial={{ opacity: 0 }} animate={{ opacity: 1 }}
                      className="text-red-400 whitespace-pre-wrap mb-4"
                    >
                      [ERROR] {error}
                    </motion.div>
                  )}
                  {output && (
                    <motion.div
                      initial={{ opacity: 0 }} animate={{ opacity: 1 }}
                      className="text-neon-emerald whitespace-pre-wrap glow-text"
                    >
                      {output}
                    </motion.div>
                  )}
                  {!output && !error && !isRunning && (
                    <div className="text-gray-600">Waiting for execution...</div>
                  )}
                </>
              )}
            </div>
          </div>
        </motion.div>
      </div>
    </main>
  );
}
