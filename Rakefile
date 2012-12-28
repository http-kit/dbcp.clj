desc "Run unit test"
task :test do
  sh 'mvn test'
end

desc "Install in local repository"
task :install_local do
  sh 'mvn install'
  # sh 'cd ~/workspace/rssminer && lein deps'
end

desc "Install in clojars repository"
task :clojars do
  sh "rm -f '*.jar' && mvn test package && cp target/*.jar ."
  sh 'scp pom.xml *.jar clojars@clojars.org:'
end
