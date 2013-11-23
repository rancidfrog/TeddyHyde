
class AndroidVersion
  def self.point( current )
    bumped = current.gsub( /^(\d+)\.(\d+)\.(\d+)$/ ) { $1 + "." + $2 + "." + ($3.to_i+1).to_s } 
    bumped
  end

  def self.major( current )

  end

  def self.minor( current )
    
  end
end
