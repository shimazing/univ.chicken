result <- data.frame()

for(angle in 4:77) {
  angleSub <- data.frame()
  for(i in levels(factor(aibirds_$X1))) {
    data <- subset(aibirds_, X1 == i & X2 == angle);
    minX <- min(data$X5);
    minY <- min(data$X6);
    angleSub <- rbind(angleSub, data.frame(
      x = data$X5 - minX,
      y = data$X6 - minY,
      nx = (data$X5 - minX) / (data$X3 + data$X4),
      ny = (data$X6 - minY) / (data$X3 + data$X4),
      level = i
    ))
  }
  modelNormal <- lm(ny ~ nx + I(nx^2), data = angleSub)
  pl <- ggplot(angleSub, aes(x = nx, y = ny, colour = factor(level))) +
    geom_point() +
    geom_line(data = data.frame(py=predict(modelNormal, 
                                           data.frame(nx=angleSub$nx)), 
                                x = angleSub$nx), aes(x = x, y = py), 
              colour="red", size=2)
  ggsave(paste("d:/desktop/aibirds/plot",angle,'.png', sep = ''), 
         plot = pl, units = 'in', width = '5.333', height = '5.333')
  
  intercept = modelNormal$coefficients[1]
  x = modelNormal$coefficients[2]
  x2 = modelNormal$coefficients[3]
  rad = atan(x)
  velocity = 1 / (sqrt(2 * x2) * cos(rad))
  result <- rbind(result, 
                  data.frame(
                    
                  rad = atan(modelNormal$coefficients[2])
                  velocity = 
                )
}

angleSub <- data.frame()
for(i in 1:7) {
  data <- subset(aibirds_, X1 == i & X2 == angle);
  minX <- min(data$X5);
  minY <- min(data$X6);
  angleSub <- rbind(angleSub, data.frame(
    x = data$X5 - minX,
    y = data$X6 - minY,
    nx = (data$X5 - minX) / (data$X3 + data$X4),
    ny = (data$X6 - minY) / (data$X3 + data$X4),
    level = i
  ))
  print(i)
}

modelNormal <- lm(ny ~ nx + I(nx^2), data = angleSub)

summary(modelNormal)

ggplot(angleSub, aes(x = nx, y = ny, colour = factor(level))) +
  geom_point() +
  geom_line(data = data.frame(py=predict(modelNormal, data.frame(nx=angleSub$nx)), x = angleSub$nx), aes(x = x, y = py), colour="red", size=2)
