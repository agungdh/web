interface SkillCardProps {
  category: string;
  icon: string;
  skills: string[];
  gradient: string;
}

export default function SkillCard({ category, icon, skills, gradient }: SkillCardProps) {
  return (
    <div className="group relative rounded-2xl border border-slate-800 bg-slate-900/50 p-6 backdrop-blur-sm hover:border-slate-700 transition-all duration-300 hover:shadow-lg hover:shadow-slate-950/50">
      <div className="flex items-center gap-3 mb-4">
        <span className="text-2xl">{icon}</span>
        <h3 className={`text-lg font-semibold text-transparent bg-clip-text bg-gradient-to-r ${gradient}`}>
          {category}
        </h3>
      </div>
      <div className="flex flex-wrap gap-2">
        {skills.map((skill) => (
          <span
            key={skill}
            className={`inline-flex items-center px-3 py-1 rounded-lg text-sm font-mono bg-slate-800 border border-slate-700 text-slate-300 hover:border-slate-600 hover:text-white transition-colors duration-200`}
          >
            {skill}
          </span>
        ))}
      </div>
    </div>
  );
}
